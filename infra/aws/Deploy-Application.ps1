[CmdletBinding()]
param(
    # This name must match the prefix used when the CloudFormation stacks were
    # created. The default produces financial-app-foundation and
    # financial-app-frontend.
    [ValidatePattern("^[a-z][a-z0-9-]{2,30}$")]
    [string]$ProjectName = "financial-app",

    # The regional foundation stack, EC2 instance, RDS database, and ECR
    # repository live here. CloudFront outputs are always read from us-east-1.
    [ValidateSet("eu-west-1", "eu-central-1", "us-east-1")]
    [string]$Region = "eu-west-1",

    # Normally omitted. The script then uses the full SHA of HEAD so that an
    # API image can always be traced back to one exact Git commit.
    [string]$ImageTag,

    # A deliberate escape hatch for a local experiment. A normal release must
    # be committed so the source, Docker image, and deployed version agree.
    [switch]$AllowDirtyWorktree
)

# Strict mode turns accidental misspellings and uninitialised variables into
# immediate errors. Stop makes a failed command halt the deployment rather than
# continuing with only part of a release completed.
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Checks that a command-line program required by this script is installed
# before the script begins making AWS or Docker changes.
function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "'$Name' is required for a manual deployment."
    }
}

# Reads a named output from a CloudFormation stack. Outputs are used instead of
# hard-coded AWS IDs so a recreated stack can still be deployed safely.
function Get-StackOutput {
    param(
        [string]$StackName,
        [string]$OutputKey,
        [string]$OutputRegion
    )

    $outputQuery = "Stacks[0].Outputs[?OutputKey=='$OutputKey'].OutputValue | [0]"
    $output = (& aws --no-cli-pager cloudformation describe-stacks `
        --region $OutputRegion `
        --stack-name $StackName `
        --query $outputQuery `
        --output text).Trim()

    if ($LASTEXITCODE -ne 0) {
        throw "Could not read outputs from CloudFormation stack '$StackName'. Run Deploy-Infrastructure.ps1 -Apply first."
    }

    if (-not $output -or $output -eq "None") {
        throw "CloudFormation stack '$StackName' has no '$OutputKey' output."
    }

    return $output
}

# SSM starts the remote command asynchronously. Poll its result until it either
# succeeds, fails, or exceeds the six-minute deployment timeout.
function Wait-ForSsmCommand {
    param(
        [string]$CommandId,
        [string]$InstanceId,
        [string]$CommandRegion
    )

    for ($attempt = 1; $attempt -le 90; $attempt++) {
        Start-Sleep -Seconds 4
        $invocation = & aws --no-cli-pager ssm get-command-invocation `
            --region $CommandRegion `
            --command-id $CommandId `
            --instance-id $InstanceId `
            --output json 2>$null

        if ($LASTEXITCODE -ne 0) {
            continue
        }

        $result = $invocation | ConvertFrom-Json
        switch ($result.Status) {
            "Success" {
                Write-Host $result.StandardOutputContent
                return
            }
            "Pending" { continue }
            "InProgress" { continue }
            "Delayed" { continue }
            default {
                Write-Host $result.StandardOutputContent
                Write-Error $result.StandardErrorContent
                throw "Remote API deployment failed with SSM status '$($result.Status)'."
            }
        }
    }

    throw "Timed out while waiting for SSM command '$CommandId'. Inspect it in Systems Manager Run Command."
}

# 1. Check local prerequisites before touching AWS. Docker builds images, Git
# provides the source revision, and AWS CLI communicates with AWS.
Require-Command aws
Require-Command docker
Require-Command git

# 2. Run from the repository root so Docker sees the expected app/ and client/
# build contexts even when this script was launched from another directory.
$repositoryRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Push-Location $repositoryRoot
try {
    # 3. A release normally starts from a clean commit. This avoids a Docker
    # image containing local edits that cannot later be recreated from Git.
    $dirtyWorktree = git status --porcelain
    if ($LASTEXITCODE -ne 0) {
        throw "Could not inspect the current Git worktree."
    }

    if ($dirtyWorktree -and -not $AllowDirtyWorktree) {
        throw "Refusing to deploy uncommitted code. Commit it first, or explicitly pass -AllowDirtyWorktree."
    }

    if (-not $ImageTag) {
        $ImageTag = (git rev-parse HEAD).Trim()
        if ($LASTEXITCODE -ne 0) {
            throw "Could not determine the Git commit used as the immutable image tag."
        }
    }

    # 4. Discover the infrastructure created by Deploy-Infrastructure.ps1.
    # No account IDs, bucket names, EC2 IDs, or database host names are stored
    # in this script; CloudFormation remains their source of truth.
    $foundationStack = "$ProjectName-foundation"
    $frontendStack = "$ProjectName-frontend"
    $apiInstanceId = Get-StackOutput -StackName $foundationStack -OutputKey "ApiInstanceId" -OutputRegion $Region
    $apiRepositoryUri = Get-StackOutput -StackName $foundationStack -OutputKey "ApiRepositoryUri" -OutputRegion $Region
    $databaseEndpoint = Get-StackOutput -StackName $foundationStack -OutputKey "DatabaseEndpointAddress" -OutputRegion $Region
    $databaseSecretArn = Get-StackOutput -StackName $foundationStack -OutputKey "DatabaseMasterSecretArn" -OutputRegion $Region
    $clientBucketName = Get-StackOutput -StackName $foundationStack -OutputKey "ClientBucketName" -OutputRegion $Region
    $clientDistributionId = Get-StackOutput -StackName $frontendStack -OutputKey "ClientDistributionId" -OutputRegion "us-east-1"
    $clientEndpoint = Get-StackOutput -StackName $frontendStack -OutputKey "ClientEndpoint" -OutputRegion "us-east-1"

    $registry = $apiRepositoryUri.Split("/")[0]
    $imageUri = "$apiRepositoryUri`:$ImageTag"

    # 5. Build the API image locally, authenticate Docker to ECR, and publish
    # it only if this immutable commit-SHA tag is not already in the registry.
    Write-Host "Building immutable API image $imageUri"
    & docker build --file "app/Dockerfile" --tag $imageUri "app"
    if ($LASTEXITCODE -ne 0) {
        throw "API image build failed."
    }

    $loginPassword = & aws --no-cli-pager ecr get-login-password --region $Region
    if ($LASTEXITCODE -ne 0) {
        throw "Could not obtain an ECR login token."
    }
    $loginPassword | & docker login --username AWS --password-stdin $registry
    if ($LASTEXITCODE -ne 0) {
        throw "Docker could not authenticate to ECR."
    }

    $repositoryName = $apiRepositoryUri.Split("/")[-1]
    # A missing immutable tag is the normal first-deployment case. In Windows
    # PowerShell, stderr from that expected nonzero AWS CLI exit otherwise
    # becomes a terminating NativeCommandError under $ErrorActionPreference.
    $previousErrorActionPreference = $ErrorActionPreference
    $imageTagExists = $false
    try {
        $ErrorActionPreference = "Continue"
        & aws --no-cli-pager ecr describe-images --region $Region --repository-name $repositoryName --image-ids "imageTag=$ImageTag" *> $null
        $imageTagExists = $LASTEXITCODE -eq 0
    }
    finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }

    if (-not $imageTagExists) {
        Write-Host "Pushing API image to ECR."
        & docker push $imageUri
        if ($LASTEXITCODE -ne 0) {
            throw "Could not push the API image to ECR."
        }
    } else {
        Write-Host "The immutable ECR image tag already exists; reusing it."
    }

    # 6. Give the EC2-side script only the non-secret deployment coordinates.
    # The instance fetches its own RDS password from Secrets Manager using its
    # IAM role, so the password does not pass through this local machine or
    # GitHub Actions.
    $deploymentConfiguration = @{
        awsRegion = $Region
        clientEndpoint = $clientEndpoint
        databaseEndpoint = $databaseEndpoint
        databaseSecretArn = $databaseSecretArn
        imageUri = $imageUri
    } | ConvertTo-Json -Compress
    $deploymentConfigurationBase64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($deploymentConfiguration))

    # The EC2-side Bash is kept in its own file so a learner can read the
    # Linux deployment steps without first decoding the SSM transport code.
    $remoteScriptTemplate = Get-Content -Raw (
        Join-Path $PSScriptRoot "Deploy-ApiOnEc2.sh"
    )
    if (-not $remoteScriptTemplate.Contains("__CONFIGURATION_BASE64__")) {
        throw "Deploy-ApiOnEc2.sh does not contain its configuration placeholder."
    }

    # The JSON has no database password. The EC2 script retrieves that secret
    # itself through its IAM role after SSM starts it.
    $remoteScript = $remoteScriptTemplate.Replace(
        "__CONFIGURATION_BASE64__",
        $deploymentConfigurationBase64
    )
    # 7. The deployment can be launched from Windows, but AWS-RunShellScript
    # executes on Linux. Normalize CRLF before Base64 encoding so Bash does not
    # receive option names such as "pipefail\r".
    $remoteScript = $remoteScript.Replace("`r`n", "`n").Replace("`r", "`n")
    $remoteScriptBase64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($remoteScript))
    $remoteCommand = "printf '%s' '$remoteScriptBase64' | base64 --decode | sudo bash"
    $ssmParametersFile = Join-Path ([System.IO.Path]::GetTempPath()) "financial-app-ssm-$([Guid]::NewGuid().ToString('N')).json"
    $ssmParameters = @{ commands = @($remoteCommand) } | ConvertTo-Json -Compress

    # Passing this large shell command as AWS CLI shorthand breaks on Windows
    # PowerShell because its quotes are parsed as shorthand syntax. A temporary
    # JSON parameter file preserves it as the one command expected by
    # AWS-RunShellScript. It contains no database password and is removed below.
    [System.IO.File]::WriteAllText(
        $ssmParametersFile,
        $ssmParameters,
        [System.Text.UTF8Encoding]::new($false)
    )

    # 8. Send the configured Linux script to EC2 through SSM. SSM uses the
    # instance role and an outbound connection, so port 22 and SSH keys are not
    # part of this deployment design.
    Write-Host "Deploying the API through Systems Manager; no SSH port or SSH key is used."
    try {
        $commandId = (& aws --no-cli-pager ssm send-command `
            --region $Region `
            --document-name "AWS-RunShellScript" `
            --instance-ids $apiInstanceId `
            --timeout-seconds 600 `
            --parameters "file://$ssmParametersFile" `
            --query "Command.CommandId" `
            --output text).Trim()
        if ($LASTEXITCODE -ne 0 -or -not $commandId) {
            throw "Could not start the Systems Manager deployment command. Ensure the instance is online in Systems Manager."
        }
    }
    finally {
        Remove-Item -LiteralPath $ssmParametersFile -Force -ErrorAction SilentlyContinue
    }
    Wait-ForSsmCommand -CommandId $commandId -InstanceId $apiInstanceId -CommandRegion $Region

    # 9. Build the static React files for the same-origin /api/v1 path. The
    # temporary output folder prevents build artifacts entering the repository.
    $frontendOutput = Join-Path ([System.IO.Path]::GetTempPath()) "financial-app-client-$([Guid]::NewGuid().ToString('N'))"
    New-Item -ItemType Directory -Path $frontendOutput | Out-Null
    try {
        Write-Host "Building the React client for its same-origin /api/v1 endpoint"
        & docker build `
            --file "client/Dockerfile" `
            --target artifacts `
            --build-arg "VITE_API_BASE_URL=/api/v1" `
            --output "type=local,dest=$frontendOutput" `
            "."
        if ($LASTEXITCODE -ne 0) {
            throw "Client build failed."
        }

        & aws --no-cli-pager s3 sync $frontendOutput "s3://$clientBucketName" `
            --delete `
            --exclude "index.html" `
            --cache-control "public,max-age=31536000,immutable"
        if ($LASTEXITCODE -ne 0) {
            throw "Could not upload static client assets."
        }

        & aws --no-cli-pager s3 cp (Join-Path $frontendOutput "index.html") "s3://$clientBucketName/index.html" `
            --cache-control "no-cache,no-store,must-revalidate" `
            --content-type "text/html"
        if ($LASTEXITCODE -ne 0) {
            throw "Could not upload the client index.html."
        }
    } finally {
        if (Test-Path -LiteralPath $frontendOutput) {
            # Cleanup must not hide an earlier build or upload failure.
            Remove-Item -LiteralPath $frontendOutput -Recurse -Force -ErrorAction SilentlyContinue
        }
    }

    # 10. CloudFront caches static files around the world. Invalidating after
    # upload makes the new HTML and assets visible without waiting for cache
    # expiry.
    & aws --no-cli-pager cloudfront create-invalidation --distribution-id $clientDistributionId --paths "/*" *> $null
    if ($LASTEXITCODE -ne 0) {
        throw "The client uploaded, but CloudFront invalidation could not be requested."
    }

    Write-Host "Deployment complete. Open $clientEndpoint and verify registration, login, and a transaction."
} finally {
    # Always restore the caller's original directory, even if deployment fails.
    Pop-Location
}

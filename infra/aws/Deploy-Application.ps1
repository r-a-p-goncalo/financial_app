[CmdletBinding()]
param(
    [ValidatePattern("^[a-z][a-z0-9-]{2,30}$")]
    [string]$ProjectName = "financial-app",

    [ValidateSet("eu-west-1", "eu-central-1", "us-east-1")]
    [string]$Region = "eu-west-1",

    [string]$ImageTag,

    [switch]$AllowDirtyWorktree
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "'$Name' is required for a manual deployment."
    }
}

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

Require-Command aws
Require-Command docker
Require-Command git

$repositoryRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Push-Location $repositoryRoot
try {
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
    & aws --no-cli-pager ecr describe-images --region $Region --repository-name $repositoryName --image-ids "imageTag=$ImageTag" *> $null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Pushing API image to ECR."
        & docker push $imageUri
        if ($LASTEXITCODE -ne 0) {
            throw "Could not push the API image to ECR."
        }
    } else {
        Write-Host "The immutable ECR image tag already exists; reusing it."
    }

    $deploymentConfiguration = @{
        awsRegion = $Region
        clientEndpoint = $clientEndpoint
        databaseEndpoint = $databaseEndpoint
        databaseSecretArn = $databaseSecretArn
        imageUri = $imageUri
    } | ConvertTo-Json -Compress
    $deploymentConfigurationBase64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($deploymentConfiguration))

    $remoteScript = @'
#!/usr/bin/env bash
set -euo pipefail

configuration="$(printf '%s' '__CONFIGURATION_BASE64__' | base64 --decode)"
image_uri="$(jq -er '.imageUri' <<< "$configuration")"
aws_region="$(jq -er '.awsRegion' <<< "$configuration")"
client_endpoint="$(jq -er '.clientEndpoint' <<< "$configuration")"
database_endpoint="$(jq -er '.databaseEndpoint' <<< "$configuration")"
database_secret_arn="$(jq -er '.databaseSecretArn' <<< "$configuration")"

sudo install -d -m 0750 /opt/financial-app/certificates

curl --fail --silent --show-error --location \
  https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem \
  --output /tmp/aws-rds-global-bundle.pem
sudo install -m 0644 /tmp/aws-rds-global-bundle.pem /opt/financial-app/certificates/aws-rds-ca.pem
rm -f /tmp/aws-rds-global-bundle.pem

database_credentials="$(aws secretsmanager get-secret-value --region "$aws_region" --secret-id "$database_secret_arn" --query SecretString --output text)"
database_username="$(jq -er '.username' <<< "$database_credentials")"
database_password="$(jq -er '.password' <<< "$database_credentials")"

sudo tee /opt/financial-app/api.env >/dev/null <<EOF
FINANCIAL_APP_DATABASE_DIALECT=postgresql
FINANCIAL_APP_DATABASE_JDBC_URL=jdbc:postgresql://${database_endpoint}:5432/financialapp?sslmode=verify-full&sslrootcert=/run/certificates/aws-rds-ca.pem
FINANCIAL_APP_DATABASE_USERNAME=${database_username}
FINANCIAL_APP_DATABASE_PASSWORD=${database_password}
FINANCIAL_APP_DATABASE_MAXIMUM_POOL_SIZE=5
FINANCIAL_APP_CORS_ALLOWED_ORIGIN=${client_endpoint}
FINANCIAL_APP_SESSION_COOKIE_SECURE=true
FINANCIAL_APP_CSRF_COOKIE_SECURE=true
EOF
sudo chmod 0600 /opt/financial-app/api.env

aws ecr get-login-password --region "$aws_region" | sudo docker login --username AWS --password-stdin "${image_uri%%/*}"
sudo docker pull "$image_uri"

sudo docker rm --force financial-app-api >/dev/null 2>&1 || true
sudo docker run --detach \
  --name financial-app-api \
  --restart unless-stopped \
  --publish 8080:8080 \
  --env-file /opt/financial-app/api.env \
  --volume /opt/financial-app/certificates/aws-rds-ca.pem:/run/certificates/aws-rds-ca.pem:ro \
  "$image_uri"

sudo docker rm --force financial-app-proxy >/dev/null 2>&1 || true

for attempt in {1..45}; do
  if curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health; then
    echo "API deployment is healthy on its CloudFront-only origin."
    exit 0
  fi
  sleep 4
done

sudo docker logs financial-app-api --tail 100
exit 1
'@
    $remoteScript = $remoteScript.Replace("__CONFIGURATION_BASE64__", $deploymentConfigurationBase64)
    $remoteScriptBase64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($remoteScript))
    $remoteCommand = "printf '%s' '$remoteScriptBase64' | base64 --decode | sudo bash"

    Write-Host "Deploying the API through Systems Manager; no SSH port or SSH key is used."
    $commandId = (& aws --no-cli-pager ssm send-command `
        --region $Region `
        --document-name "AWS-RunShellScript" `
        --instance-ids $apiInstanceId `
        --timeout-seconds 600 `
        --parameters "commands=$remoteCommand" `
        --query "Command.CommandId" `
        --output text).Trim()
    if ($LASTEXITCODE -ne 0 -or -not $commandId) {
        throw "Could not start the Systems Manager deployment command. Ensure the instance is online in Systems Manager."
    }
    Wait-ForSsmCommand -CommandId $commandId -InstanceId $apiInstanceId -CommandRegion $Region

    $frontendOutput = Join-Path ([System.IO.Path]::GetTempPath()) "financial-app-client-$([Guid]::NewGuid().ToString('N'))"
    New-Item -ItemType Directory -Path $frontendOutput | Out-Null
    try {
        Write-Host "Building the React client for its same-origin /api/v1 endpoint"
        & docker build `
            --file "client/Dockerfile" `
            --target build `
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
            Remove-Item -LiteralPath $frontendOutput -Recurse -Force
        }
    }

    & aws --no-cli-pager cloudfront create-invalidation --distribution-id $clientDistributionId --paths "/*" *> $null
    if ($LASTEXITCODE -ne 0) {
        throw "The client uploaded, but CloudFront invalidation could not be requested."
    }

    Write-Host "Deployment complete. Open $clientEndpoint and verify registration, login, and a transaction."
} finally {
    Pop-Location
}

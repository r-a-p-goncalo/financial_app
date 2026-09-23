[CmdletBinding()]
param(
    # The common prefix for the two CloudFormation stack names. Keeping one
    # name here lets a learner create a separate sandbox without editing YAML.
    [ValidatePattern("^[a-z][a-z0-9-]{2,30}$")]
    [string]$ProjectName = "financial-app",

    # AWS Region for resources that are regional (VPC, EC2, RDS, S3, and ECR).
    # CloudFront's companion stack is deliberately deployed in us-east-1 below.
    [ValidateSet("eu-west-1", "eu-central-1", "us-east-1")]
    [string]$Region = "eu-west-1",

    # this was necessary due to free tier
    # One day is the inexpensive learning-project setting. Choose a longer
    # retention period before the database holds data that cannot be replaced.
    [ValidateRange(1, 35)]
    [int]$DatabaseBackupRetentionDays = 1,

    # this can be run with -Apply
    # Without this switch, the script validates only. This makes the default
    # command safe to use when learning or reviewing template changes.
    [switch]$Apply
)

Set-StrictMode -Version Latest
# Stop immediately when AWS rejects a command. Without this, a later command
# could run with missing stack output and make the error harder to understand.
$ErrorActionPreference = "Stop" #variable

#to check if there is a command that the script needs and does not have access to
function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "'$Name' is required. Install it and configure AWS credentials before continuing."
    }
}

#calls aws cli
function Invoke-Aws {
    param([string[]]$Arguments)

    & aws --no-cli-pager @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "AWS CLI command failed: aws $($Arguments -join ' ')"
    }
}

# Reads a value that CloudFormation produced after a successful stack
# deployment. This is safer than copying bucket names or instance host names
# into the script by hand.
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
        throw "Could not read outputs from CloudFormation stack '$StackName'."
    }

    if (-not $output -or $output -eq "None") {
        throw "CloudFormation stack '$StackName' has no '$OutputKey' output."
    }

    return $output
}

# CloudFormation can fail before it creates a resource event. In that case the
# failed change set, rather than normal stack events, contains the useful
# template-property error.
function Show-ChangeSetValidationFailures {
    param(
        [string]$StackName,
        [string]$StackRegion
    )

    # `cloudformation deploy` creates a timestamped change set. If its
    # pre-deployment property validation fails, describe-stack-events only
    # reports REVIEW_IN_PROGRESS; the precise failure is attached to that
    # change set instead.
    $changeSetName = (& aws --no-cli-pager cloudformation list-change-sets `
        --region $StackRegion `
        --stack-name $StackName `
        --query "reverse(sort_by(Summaries[?Status=='FAILED'], &CreationTime))[0].ChangeSetName" `
        --output text).Trim()

    if ($LASTEXITCODE -ne 0 -or -not $changeSetName -or $changeSetName -eq "None") {
        return
    }

    Write-Warning "CloudFormation validation details for change set '$changeSetName':"
    & aws --no-cli-pager cloudformation describe-events `
        --region $StackRegion `
        --stack-name $StackName `
        --change-set-name $changeSetName `
        --filters "FailedEvents=true" `
        --query "OperationEvents[?EventType=='VALIDATION_ERROR'].[LogicalResourceId,ResourceType,ValidationStatusReason,ValidationPath]" `
        --output table
}

# we first check if we have access to aws cli
Require-Command aws

# The foundation is regional because it contains VPC, EC2, RDS, S3, and ECR.
# The frontend stack is in us-east-1 because CloudFront is a global service and
# this project deliberately keeps its CloudFront resources in one fixed region.
$foundationTemplate = Join-Path $PSScriptRoot "foundation.yaml"
$frontendTemplate = Join-Path $PSScriptRoot "frontend.yaml"
$foundationStack = "$ProjectName-foundation"
$frontendStack = "$ProjectName-frontend"

# 1. Validate both templates first. This only asks AWS whether the YAML and
# CloudFormation syntax are valid; it does not create, update, or delete any
# resources.
# Template validation is intentionally read-only and runs even without -Apply
Invoke-Aws @("cloudformation", "validate-template", "--region", $Region, "--template-body", "file://$foundationTemplate")
Invoke-Aws @("cloudformation", "validate-template", "--region", "us-east-1", "--template-body", "file://$frontendTemplate")

#if we're not running apply, we stop at checking if the template is valid
if (-not $Apply) {
    Write-Host "Templates are valid. No AWS resources were created or changed."
    Write-Host "Review the templates, then rerun with -Apply to create or update:"
    Write-Host "  $foundationStack in $Region"
    Write-Host "  $frontendStack in us-east-1"
    Write-Host "The frontend stack uses CloudFront's generated HTTPS hostname; no domain or Route 53 hosted zone is required."
    return
}

# 2. The API security group needs AWS's managed list of CloudFront origin IP
# ranges. Query it rather than copying a value, because AWS owns this list.
#we get the cloud front prefix
$cloudFrontPrefixListId = (& aws --no-cli-pager ec2 describe-managed-prefix-lists `
    --region $Region `
    --filters "Name=prefix-list-name,Values=com.amazonaws.global.cloudfront.origin-facing" `
    --query "PrefixLists[0].PrefixListId" `
    --output text).Trim()
if ($LASTEXITCODE -ne 0 -or -not $cloudFrontPrefixListId -or $cloudFrontPrefixListId -eq "None") {
    throw "Could not find the AWS-managed CloudFront origin-facing prefix list in $Region."
}

# 3. Deploy the regional foundation. CAPABILITY_NAMED_IAM is required because
# foundation.yaml creates named IAM roles and the GitHub deployment user.
# we get the foundation parameters and deploy it

$foundationParameters = @(
    "ProjectName=$ProjectName",
    "CloudFrontOriginPrefixListId=$cloudFrontPrefixListId",
    "DatabaseBackupRetentionDays=$DatabaseBackupRetentionDays"
)

$foundationDeploymentArguments = @(
    "cloudformation", "deploy",
    "--region", $Region,
    "--stack-name", $foundationStack,
    "--template-file", $foundationTemplate,
    "--parameter-overrides"
) + $foundationParameters + @(
    "--capabilities", "CAPABILITY_NAMED_IAM",
    "--no-fail-on-empty-changeset",
    "--tags", "Project=$ProjectName", "ManagedBy=CloudFormation"
)
Invoke-Aws $foundationDeploymentArguments

# 4. Read the foundation outputs needed by CloudFront. CloudFormation passes
# these values to the frontend stack so the two templates stay independent.
$bucketName = Get-StackOutput -StackName $foundationStack -OutputKey "ClientBucketName" -OutputRegion $Region
$bucketDomain = Get-StackOutput -StackName $foundationStack -OutputKey "ClientBucketRegionalDomainName" -OutputRegion $Region
$apiOriginDomainName = Get-StackOutput -StackName $foundationStack -OutputKey "ApiOriginDomainName" -OutputRegion $Region

try {
    # 5. Deploy the CloudFront/S3 frontend stack in us-east-1. It creates the
    # public HTTPS endpoint, allows CloudFront to read the private bucket, and
    # forwards /api/* to the EC2 API origin.
    Invoke-Aws @(
        "cloudformation", "deploy",
        "--region", "us-east-1",
        "--stack-name", $frontendStack,
        "--template-file", $frontendTemplate,
        "--parameter-overrides",
        "ClientBucketName=$bucketName",
        "ClientBucketRegionalDomainName=$bucketDomain",
        "ApiOriginDomainName=$apiOriginDomainName",
        "--no-fail-on-empty-changeset",
        "--tags", "Project=$ProjectName", "ManagedBy=CloudFormation"
    )
}
catch {
    Show-ChangeSetValidationFailures -StackName $frontendStack -StackRegion "us-east-1"
    throw
}

# 6. Print only safe, useful next-step values. Database credentials are never
# printed: RDS stores them in Secrets Manager for the EC2 role to retrieve.
Write-Host "Infrastructure is ready. These outputs identify the next manual deployment target:"
Write-Host "  Client: $(Get-StackOutput -StackName $frontendStack -OutputKey 'ClientEndpoint' -OutputRegion 'us-east-1')"
Write-Host "  API:    served from the Client URL at /api/v1 (CloudFront only)"
Write-Host "  GitHub deployment user: $(Get-StackOutput -StackName $foundationStack -OutputKey 'GitHubActionsDeploymentUserName' -OutputRegion $Region)"
Write-Host "Run Deploy-Application.ps1 manually when you are ready to publish an application version."

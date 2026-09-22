[CmdletBinding()]
param(
    [ValidatePattern("^[a-z][a-z0-9-]{2,30}$")]
    [string]$ProjectName = "financial-app",

    [ValidateSet("eu-west-1", "eu-central-1", "us-east-1")]
    [string]$Region = "eu-west-1",

    [ValidateRange(1, 35)]
    [int]$DatabaseBackupRetentionDays = 1,

    [switch]$Apply
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "'$Name' is required. Install it and configure AWS credentials before continuing."
    }
}

function Invoke-Aws {
    param([string[]]$Arguments)

    & aws --no-cli-pager @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "AWS CLI command failed: aws $($Arguments -join ' ')"
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
        throw "Could not read outputs from CloudFormation stack '$StackName'."
    }

    if (-not $output -or $output -eq "None") {
        throw "CloudFormation stack '$StackName' has no '$OutputKey' output."
    }

    return $output
}

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

Require-Command aws

$foundationTemplate = Join-Path $PSScriptRoot "foundation.yaml"
$frontendTemplate = Join-Path $PSScriptRoot "frontend.yaml"
$foundationStack = "$ProjectName-foundation"
$frontendStack = "$ProjectName-frontend"

# Template validation is intentionally read-only and runs even without -Apply.
Invoke-Aws @("cloudformation", "validate-template", "--region", $Region, "--template-body", "file://$foundationTemplate")
Invoke-Aws @("cloudformation", "validate-template", "--region", "us-east-1", "--template-body", "file://$frontendTemplate")

if (-not $Apply) {
    Write-Host "Templates are valid. No AWS resources were created or changed."
    Write-Host "Review the templates, then rerun with -Apply to create or update:"
    Write-Host "  $foundationStack in $Region"
    Write-Host "  $frontendStack in us-east-1"
    Write-Host "The frontend stack uses CloudFront's generated HTTPS hostname; no domain or Route 53 hosted zone is required."
    return
}

$cloudFrontPrefixListId = (& aws --no-cli-pager ec2 describe-managed-prefix-lists `
    --region $Region `
    --filters "Name=prefix-list-name,Values=com.amazonaws.global.cloudfront.origin-facing" `
    --query "PrefixLists[0].PrefixListId" `
    --output text).Trim()
if ($LASTEXITCODE -ne 0 -or -not $cloudFrontPrefixListId -or $cloudFrontPrefixListId -eq "None") {
    throw "Could not find the AWS-managed CloudFront origin-facing prefix list in $Region."
}

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

$bucketName = Get-StackOutput -StackName $foundationStack -OutputKey "ClientBucketName" -OutputRegion $Region
$bucketDomain = Get-StackOutput -StackName $foundationStack -OutputKey "ClientBucketRegionalDomainName" -OutputRegion $Region
$apiOriginDomainName = Get-StackOutput -StackName $foundationStack -OutputKey "ApiOriginDomainName" -OutputRegion $Region

try {
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

Write-Host "Infrastructure is ready. These outputs identify the next manual deployment target:"
Write-Host "  Client: $(Get-StackOutput -StackName $frontendStack -OutputKey 'ClientEndpoint' -OutputRegion 'us-east-1')"
Write-Host "  API:    served from the Client URL at /api/v1 (CloudFront only)"
Write-Host "  GitHub deployment user: $(Get-StackOutput -StackName $foundationStack -OutputKey 'GitHubActionsDeploymentUserName' -OutputRegion $Region)"
Write-Host "Run Deploy-Application.ps1 manually when you are ready to publish an application version."

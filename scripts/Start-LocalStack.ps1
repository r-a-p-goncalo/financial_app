[CmdletBinding()]
param(
    [switch]$InstallDependencies,

    [string]$BootstrapConfigFile = "config/bootstrap/financial-demo.json",

    [switch]$ResetDatabase
)

$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$apiDirectory = Join-Path $repositoryRoot "app"
$clientDirectory = Join-Path $repositoryRoot "client"
$clientNodeModules = Join-Path $clientDirectory "node_modules"
$localDatabasePath = Join-Path $apiDirectory "data/financial-app.db"
$localDatabaseDirectory = Split-Path -Parent $localDatabasePath
$bootstrapConfigPath = if ([System.IO.Path]::IsPathRooted($BootstrapConfigFile)) {
    $BootstrapConfigFile
} else {
    Join-Path $apiDirectory $BootstrapConfigFile
}

if (-not (Get-Command pwsh -ErrorAction SilentlyContinue)) {
    throw "PowerShell 7 (pwsh) is required to open the API and client terminals."
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Apache Maven is required to run the API. Install it, reopen PowerShell, and run this script again."
}

if (-not (Test-Path -LiteralPath $bootstrapConfigPath -PathType Leaf)) {
    throw "Bootstrap configuration does not exist: $bootstrapConfigPath"
}

if (-not (Test-Path -LiteralPath $localDatabaseDirectory -PathType Container)) {
    New-Item -ItemType Directory -Path $localDatabaseDirectory | Out-Null
}

if ($ResetDatabase) {
    if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
        throw "Stop the API listening on port 8080 before resetting its local database."
    }

    foreach ($databaseFile in @(
            $localDatabasePath,
            "${localDatabasePath}-journal",
            "${localDatabasePath}-shm",
            "${localDatabasePath}-wal"
    )) {
        if (Test-Path -LiteralPath $databaseFile -PathType Leaf) {
            Remove-Item -LiteralPath $databaseFile -Force
            Write-Host "Removed local database file: $databaseFile"
        }
    }
}

if ($InstallDependencies) {
    Push-Location $clientDirectory
    try {
        & corepack pnpm install --frozen-lockfile
        if ($LASTEXITCODE -ne 0) {
            throw "Client dependency installation failed."
        }
    } finally {
        Pop-Location
    }
} elseif (-not (Test-Path -LiteralPath $clientNodeModules -PathType Container)) {
    throw "Client dependencies are missing. Run this script again with -InstallDependencies."
}

$localApiEnvironment = [ordered]@{
    "SPRING_PROFILES_ACTIVE" = "bootstrap"
    "FINANCIAL_APP_BOOTSTRAP_ENABLED" = "true"
    "FINANCIAL_APP_BOOTSTRAP_CONFIG_FILE" = $BootstrapConfigFile
    "FINANCIAL_APP_DATABASE_DIALECT" = "sqlite"
    "FINANCIAL_APP_DATABASE_JDBC_URL" = "jdbc:sqlite:data/financial-app.db"
    "FINANCIAL_APP_CORS_ALLOWED_ORIGIN" = "http://localhost:5173"
    "FINANCIAL_APP_SESSION_COOKIE_SECURE" = "false"
    "FINANCIAL_APP_CSRF_COOKIE_SECURE" = "false"
}
$previousApiEnvironment = @{}

try {
    foreach ($environmentName in $localApiEnvironment.Keys) {
        $previousApiEnvironment[$environmentName] =
            [System.Environment]::GetEnvironmentVariable(
                    $environmentName,
                    "Process"
            )
        [System.Environment]::SetEnvironmentVariable(
                $environmentName,
                $localApiEnvironment[$environmentName],
                "Process"
        )
    }

    $apiTerminal = Start-Process `
        -FilePath "pwsh" `
        -WorkingDirectory $apiDirectory `
        -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run" `
        -PassThru
} finally {
    foreach ($environmentName in $localApiEnvironment.Keys) {
        [System.Environment]::SetEnvironmentVariable(
                $environmentName,
                $previousApiEnvironment[$environmentName],
                "Process"
        )
    }
}

$clientTerminal = Start-Process `
    -FilePath "pwsh" `
    -WorkingDirectory $clientDirectory `
    -ArgumentList "-NoExit", "-Command", "corepack pnpm dev" `
    -PassThru

Write-Host "Started API terminal (PID $($apiTerminal.Id)) and client terminal (PID $($clientTerminal.Id))."
Write-Host "Open http://localhost:5173 after the API reports that it is listening on port 8080."
Write-Host "The API uses app/data/financial-app.db and seeds $BootstrapConfigFile for demo/demo-password."

[CmdletBinding()]
param(
    [switch]$InstallDependencies
)

$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$apiDirectory = Join-Path $repositoryRoot "app"
$clientDirectory = Join-Path $repositoryRoot "client"
$clientNodeModules = Join-Path $clientDirectory "node_modules"

if (-not (Get-Command pwsh -ErrorAction SilentlyContinue)) {
    throw "PowerShell 7 (pwsh) is required to open the API and client terminals."
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Apache Maven is required to run the API. Install it, reopen PowerShell, and run this script again."
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

$apiTerminal = Start-Process `
    -FilePath "pwsh" `
    -WorkingDirectory $apiDirectory `
    -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run" `
    -PassThru

$clientTerminal = Start-Process `
    -FilePath "pwsh" `
    -WorkingDirectory $clientDirectory `
    -ArgumentList "-NoExit", "-Command", "corepack pnpm dev" `
    -PassThru

Write-Host "Started API terminal (PID $($apiTerminal.Id)) and client terminal (PID $($clientTerminal.Id))."
Write-Host "Open http://localhost:5173 after the API reports that it is listening on port 8080."
Write-Host "The API uses its default local SQLite database in app/data/financial-app.db."

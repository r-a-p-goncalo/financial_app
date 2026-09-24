[CmdletBinding()]
param(
    [ValidatePattern("^https://")]
    [string]$ApiProxyTarget,

    [switch]$InstallDependencies
)

$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$clientDirectory = Join-Path $repositoryRoot "client"
$remoteEnvironmentFile = Join-Path $clientDirectory ".env.remote"

if ($ApiProxyTarget) {
    $env:VITE_API_PROXY_TARGET = $ApiProxyTarget
} elseif (-not (Test-Path -LiteralPath $remoteEnvironmentFile -PathType Leaf)) {
    throw "Set VITE_API_PROXY_TARGET in client/.env.remote or pass -ApiProxyTarget https://your-distribution.cloudfront.net."
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
} elseif (-not (Test-Path -LiteralPath (Join-Path $clientDirectory "node_modules") -PathType Container)) {
    throw "Client dependencies are missing. Run this script again with -InstallDependencies."
}

Set-Location $clientDirectory
& corepack pnpm dev -- --mode remote

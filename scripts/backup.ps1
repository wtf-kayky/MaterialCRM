param(
    [string]$OutputDirectory = ".\backups"
)

$ErrorActionPreference = "Stop"
$projectDirectory = Split-Path -Parent $PSScriptRoot
Set-Location $projectDirectory

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupFile = Join-Path $OutputDirectory "materialcrm-$timestamp.sql"

docker compose exec -T database pg_dump `
    -U materialcrm `
    -d materialcrm `
    --clean `
    --if-exists | Out-File -FilePath $backupFile -Encoding utf8

Write-Host "Backup criado em: $backupFile"

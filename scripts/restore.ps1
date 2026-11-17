param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile
)

$ErrorActionPreference = "Stop"
$projectDirectory = Split-Path -Parent $PSScriptRoot
Set-Location $projectDirectory

if (-not (Test-Path $BackupFile)) {
    throw "Arquivo de backup não encontrado: $BackupFile"
}

Write-Host "A restauração substituirá os dados atuais do banco."
$confirmation = Read-Host "Digite RESTAURAR para continuar"
if ($confirmation -ne "RESTAURAR") {
    Write-Host "Restauração cancelada."
    exit 0
}

Get-Content -Raw $BackupFile | docker compose exec -T database psql -U materialcrm -d materialcrm
Write-Host "Restauração concluída."

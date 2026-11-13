$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
Push-Location (Join-Path $raiz 'backend')
try {
    docker compose -f docker-compose.render-local.yml down
}
finally {
    Pop-Location
}
Write-Host "Contenedores del perfil Render local detenidos correctamente." -ForegroundColor Green

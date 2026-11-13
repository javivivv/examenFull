$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot

Write-Host "COMPILACION Y PRUEBA LOCAL DEL PERFIL RENDER (GameGo)" -ForegroundColor Cyan
Write-Host "1/3 Compilando el backend con Maven..." -ForegroundColor Yellow
Push-Location (Join-Path $raiz 'backend')
try {
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven no logro generar los JAR." }

    Write-Host "2/3 Construyendo y levantando contenedores..." -ForegroundColor Yellow
    docker compose -f docker-compose.render-local.yml up --build -d
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose no logro levantar la solución." }

    Write-Host "3/3 Estado de los contenedores:" -ForegroundColor Yellow
    docker compose -f docker-compose.render-local.yml ps
}
finally {
    Pop-Location
}

Write-Host "`nPruebas sugeridas (backend):" -ForegroundColor Green
Write-Host "GET http://localhost:8080/api/productos/"
Write-Host "POST http://localhost:8080/api/clientes/login   {""email"":""demo@gmail.com"",""password"":""Demo1234""}"
Write-Host "`nFrontend (en otra terminal, desde la raiz del proyecto):" -ForegroundColor Green
Write-Host "npm install"
Write-Host "npm run dev      (usa http://localhost:8080 como API por defecto)"
Write-Host "`nPara detener: .\scripts\03_detener_render_local.ps1" -ForegroundColor Gray

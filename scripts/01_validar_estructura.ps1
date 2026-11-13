$ErrorActionPreference = "SilentlyContinue"
$script:HayErrores = $false

function Mostrar-Resultado {
    param(
        [string]$Nombre,
        [bool]$Correcto,
        [string]$Detalle
    )

    if ($Correcto) {
        Write-Host "[OK]    $Nombre - $Detalle" -ForegroundColor Green
    }
    else {
        Write-Host "[ERROR] $Nombre - $Detalle" -ForegroundColor Red
        $script:HayErrores = $true
    }
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " VALIDACION DE ESTRUCTURA - GAMEGO" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host ""

$raizProyecto = Split-Path -Parent $PSScriptRoot
Set-Location $raizProyecto

Write-Host "1. Verificacion de archivos principales" -ForegroundColor Cyan
Write-Host ""

$archivosPrincipales = @(
    "package.json",
    "vite.config.mjs",
    "render.yaml",
    "render.yaml.template",
    "backend\pom.xml",
    "backend\docker-compose.render-local.yml",
    "postman_render_online.json"
)

foreach ($archivo in $archivosPrincipales) {
    if (Test-Path $archivo) {
        Mostrar-Resultado $archivo $true "Encontrado"
    }
    else {
        Mostrar-Resultado $archivo $false "Archivo faltante"
    }
}

Write-Host ""
Write-Host "2. Verificacion de carpetas principales" -ForegroundColor Cyan
Write-Host ""

$carpetas = @(
    "src",
    "public\imagenes",
    "backend\api-gateway",
    "backend\cliente-service",
    "backend\producto-service",
    "backend\pedido-service",
    "backend\pago-service",
    "backend\despacho-service",
    "backend\eureka-server",
    "backend\render",
    "backend\render\docker",
    "scripts"
)

foreach ($carpeta in $carpetas) {
    if (Test-Path $carpeta) {
        Mostrar-Resultado $carpeta $true "Encontrada"
    }
    else {
        Mostrar-Resultado $carpeta $false "Carpeta faltante"
    }
}

Write-Host ""
Write-Host "3. Verificacion de Dockerfiles para Render" -ForegroundColor Cyan
Write-Host ""

$dockerfiles = @(
    "backend\render\docker\api-gateway.Dockerfile",
    "backend\render\docker\cliente-service.Dockerfile",
    "backend\render\docker\producto-service.Dockerfile",
    "backend\render\docker\pedido-service.Dockerfile",
    "backend\render\docker\pago-service.Dockerfile",
    "backend\render\docker\despacho-service.Dockerfile"
)

foreach ($dockerfile in $dockerfiles) {
    if (Test-Path $dockerfile) {
        Mostrar-Resultado $dockerfile $true "Encontrado"
    }
    else {
        Mostrar-Resultado $dockerfile $false "Dockerfile faltante"
    }
}

Write-Host ""
Write-Host "4. Verificacion basica de render.yaml" -ForegroundColor Cyan
Write-Host ""

if (Test-Path "render.yaml") {
    $contenido = Get-Content "render.yaml" -Raw

    if ($contenido -match "TU-PREFIJO") {
        Mostrar-Resultado "Prefijo personalizado" $false "Todavia existe el texto TU-PREFIJO."
    }
    else {
        Mostrar-Resultado "Prefijo personalizado" $true "El prefijo fue reemplazado."
    }

    if ($contenido -match "services:") {
        Mostrar-Resultado "services:" $true "Seccion principal encontrada."
    }
    else {
        Mostrar-Resultado "services:" $false "No se encontro la seccion services."
    }

    if ($contenido -match "dockerfilePath") {
        Mostrar-Resultado "dockerfilePath" $true "Rutas de Dockerfiles encontradas."
    }
    else {
        Mostrar-Resultado "dockerfilePath" $false "No se encontraron rutas de Dockerfiles."
    }
}
else {
    Mostrar-Resultado "render.yaml" $false "Debes ejecutar configurar-render.ps1."
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Cyan

if ($script:HayErrores) {
    Write-Host "RESULTADO: HAY ERRORES EN LA ESTRUCTURA." -ForegroundColor Yellow
    Write-Host "Corrige los elementos marcados antes de continuar." -ForegroundColor Yellow
    exit 1
}
else {
    Write-Host "RESULTADO: ESTRUCTURA VALIDADA CORRECTAMENTE." -ForegroundColor Green
    Write-Host "Puedes continuar con la prueba local antes de subir a GitHub." -ForegroundColor Green
    exit 0
}

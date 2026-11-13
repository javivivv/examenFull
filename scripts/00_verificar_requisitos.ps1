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

function Probar-Comando {
    param(
        [string]$Nombre,
        [string]$Comando,
        [string[]]$Argumentos,
        [string]$MensajeError
    )

    $existe = Get-Command $Comando -ErrorAction SilentlyContinue

    if (-not $existe) {
        Mostrar-Resultado $Nombre $false $MensajeError
        return
    }

    $global:LASTEXITCODE = 0
    $salida = & $Comando @Argumentos 2>&1 | Out-String

    if ($LASTEXITCODE -eq 0) {
        $primeraLinea = ($salida.Trim() -split "`r?`n")[0]
        Mostrar-Resultado $Nombre $true $primeraLinea
    }
    else {
        Mostrar-Resultado $Nombre $false $MensajeError
    }
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " CHECKLIST PREVIO - GAMEGO" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host ""

$raizProyecto = Split-Path -Parent $PSScriptRoot
Set-Location $raizProyecto

Write-Host "1. Verificacion de herramientas" -ForegroundColor Cyan
Write-Host ""

Probar-Comando "Java" "java" @("-version") "Java no esta disponible. Instala o configura JDK 21."
Probar-Comando "Maven" "mvn" @("-version") "Maven no esta disponible o no esta configurado en PATH."
Probar-Comando "Node.js" "node" @("--version") "Node.js no esta disponible. Instala Node 20.19+ o 22.12+ (requerido por Vite)."
Probar-Comando "npm" "npm" @("--version") "npm no esta disponible."
Probar-Comando "Git" "git" @("--version") "Git no esta disponible. Instala Git for Windows."
Probar-Comando "Docker CLI" "docker" @("--version") "Docker CLI no esta disponible. Instala Docker Desktop."
Probar-Comando "Docker Compose" "docker" @("compose", "version") "Docker Compose no esta disponible."

Write-Host ""
Write-Host "2. Verificacion de Docker Engine" -ForegroundColor Cyan
Write-Host ""

$dockerExiste = Get-Command docker -ErrorAction SilentlyContinue

if ($dockerExiste) {
    $global:LASTEXITCODE = 0
    $dockerInfo = docker info 2>&1 | Out-String

    if ($LASTEXITCODE -eq 0) {
        Mostrar-Resultado "Docker Engine" $true "Servidor Docker activo"
    }
    else {
        Mostrar-Resultado "Docker Engine" $false "Docker Desktop no esta iniciado o Engine no responde."
    }
}
else {
    Mostrar-Resultado "Docker Engine" $false "Docker CLI no esta instalado."
}

Write-Host ""
Write-Host "3. Verificacion de archivos obligatorios" -ForegroundColor Cyan
Write-Host ""

$archivos = @(
    "package.json",
    "render.yaml.template",
    "backend\pom.xml",
    "backend\docker-compose.render-local.yml",
    "postman_render_online.json",
    "scripts\configurar-render.ps1",
    "scripts\01_validar_estructura.ps1",
    "scripts\02_probar_render_local.ps1",
    "scripts\03_detener_render_local.ps1"
)

foreach ($archivo in $archivos) {
    if (Test-Path $archivo) {
        Mostrar-Resultado $archivo $true "Encontrado"
    }
    else {
        Mostrar-Resultado $archivo $false "Archivo faltante"
    }
}

Write-Host ""
Write-Host "4. Verificacion de carpetas del proyecto" -ForegroundColor Cyan
Write-Host ""

$carpetas = @(
    "src",
    "public",
    "backend\api-gateway",
    "backend\cliente-service",
    "backend\producto-service",
    "backend\pedido-service",
    "backend\pago-service",
    "backend\despacho-service",
    "backend\eureka-server",
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
Write-Host "==========================================================" -ForegroundColor Cyan

if ($script:HayErrores) {
    Write-Host "RESULTADO: HAY ERRORES." -ForegroundColor Yellow
    Write-Host "Corrige los elementos marcados antes de continuar." -ForegroundColor Yellow
    exit 1
}
else {
    Write-Host "RESULTADO: CHECKLIST COMPLETADO CORRECTAMENTE." -ForegroundColor Green
    Write-Host "Puedes continuar con la siguiente etapa." -ForegroundColor Green
    exit 0
}
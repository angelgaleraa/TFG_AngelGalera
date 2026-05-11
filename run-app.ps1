$ErrorActionPreference = "Stop"

$backendScript = Join-Path $PSScriptRoot "run-backend.ps1"
$frontendScript = Join-Path $PSScriptRoot "run-frontend.ps1"

Write-Host "Arrancando backend con base de datos cloud..."
$backend = Start-Process powershell `
    -ArgumentList "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", "`"$backendScript`"", "cloud" `
    -WorkingDirectory $PSScriptRoot `
    -PassThru

Write-Host "Esperando a que el backend responda en http://localhost:8081 ..."
$ready = $false
for ($i = 0; $i -lt 60; $i++) {
    if ($backend.HasExited) {
        throw "El backend se ha cerrado antes de arrancar. Revisa la ventana del backend."
    }
    try {
        Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:8081/api/items?page=0&size=1" -TimeoutSec 2 | Out-Null
        $ready = $true
        break
    } catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $ready) {
    throw "El backend no ha respondido a tiempo. Revisa la ventana del backend."
}

Write-Host "Backend listo. Arrancando frontend..."
& powershell -NoProfile -ExecutionPolicy Bypass -File $frontendScript

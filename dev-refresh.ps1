# Script de limpieza profunda y reinicio para StockDual (Vaadin + Spring Boot)
# Este script fuerza la regeneración de los bundles de Vite y limpia caches persistentes.

Write-Host "--- Deteniendo instancias de Java existentes ---" -ForegroundColor Cyan
Stop-Process -Name "java" -ErrorAction SilentlyContinue

Write-Host "--- Eliminando carpetas de cache y archivos generados ---" -ForegroundColor Yellow
# target: Archivos compilados de Java y bundles previos
# node_modules/.vite: Cache persistente de Vite
# src/main/frontend/generated: Archivos generados de Vaadin
$folders = "target", "node_modules/.vite", "src/main/frontend/generated"
foreach ($folder in $folders) {
    if (Test-Path $folder) {
        Remove-Item -Recurse -Force $folder
        Write-Host "Eliminado: $folder"
    }
}

Write-Host "--- Limpiando archivos temporales de Vaadin ---" -ForegroundColor Yellow
$vaadinTemp = "vaadin-dev-bundle"
if (Test-Path $vaadinTemp) {
    Remove-Item -Recurse -Force $vaadinTemp
    Write-Host "Eliminado: $vaadinTemp"
}

Write-Host "--- Iniciando aplicación con limpieza de Maven ---" -ForegroundColor Green
./mvnw.cmd clean spring-boot:run

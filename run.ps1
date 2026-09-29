# PowerShell launch script for Hostel Mess Daily Menu Register

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Hostel Mess Daily Menu Register" -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling Java code..." -ForegroundColor Green
$javaFiles = Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName
javac -cp "lib/*;src" -d "bin" $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[2/2] Launching Application GUI..." -ForegroundColor Green
Start-Process "javaw" -ArgumentList "-cp", "lib/*;bin", "com.hostel.mess.AppMain"
Write-Host "Application running!" -ForegroundColor Cyan

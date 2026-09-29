@echo off
title Hostel Mess Daily Menu - Web View
echo ========================================================
echo   Launching Hostel Mess Web Client Portal in Browser
echo ========================================================
echo.

taskkill /F /IM javaw.exe 2>nul
taskkill /F /IM java.exe 2>nul

if not exist bin (
    mkdir bin
)

echo [1/2] Compiling...
javac -encoding UTF-8 -cp "lib/*;src" -d "bin" src\com\hostel\mess\model\*.java src\com\hostel\mess\exception\*.java src\com\hostel\mess\dao\*.java src\com\hostel\mess\database\*.java src\com\hostel\mess\util\*.java src\com\hostel\mess\web\*.java src\com\hostel\mess\view\*.java src\com\hostel\mess\*.java

echo [2/2] Starting Server and Opening Browser...
start javaw -cp "lib/*;bin" com.hostel.mess.AppMain
timeout /t 2 >nul
start http://localhost:8080/?day=Monday
echo Web client opened at http://localhost:8080/?day=Monday
exit /b 0

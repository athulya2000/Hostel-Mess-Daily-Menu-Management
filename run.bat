@echo off
title Hostel Mess Daily Menu Register
echo ========================================================
echo   Hostel Mess Daily Menu Register
echo ========================================================
echo.

taskkill /F /IM javaw.exe 2>nul
taskkill /F /IM java.exe 2>nul

if not exist bin (
    mkdir bin
)

echo [1/2] Compiling Java source files...
javac -encoding UTF-8 -cp "lib/*;src" -d "bin" src\com\hostel\mess\model\*.java src\com\hostel\mess\exception\*.java src\com\hostel\mess\dao\*.java src\com\hostel\mess\database\*.java src\com\hostel\mess\util\*.java src\com\hostel\mess\web\*.java src\com\hostel\mess\view\*.java src\com\hostel\mess\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed! Please check Java JDK installation.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching Application GUI...
start javaw -cp "lib/*;bin" com.hostel.mess.AppMain
echo Application started successfully.
exit /b 0

@echo off
setlocal
title RailConnect - MySQL and Spring Boot

rem Vehicle Rental-style defaults. Edit these if your local MySQL differs.
set "MYSQL_USER=root"
set "MYSQL_PASSWORD=root"
set "MYSQL_HOST=127.0.0.1"
set "MYSQL_PORT=3306"
set "DATABASE_NAME=railconnect"

where mysql >nul 2>nul
if %errorlevel%==0 (
    set "MYSQL_COMMAND=mysql"
) else if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set "MYSQL_COMMAND=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
) else (
    echo MySQL was not found. Install MySQL Server and add its bin folder to PATH.
    pause
    exit /b 1
)

echo Creating database %DATABASE_NAME% if it does not exist...
"%MYSQL_COMMAND%" --host=%MYSQL_HOST% --port=%MYSQL_PORT% --user=%MYSQL_USER% --password=%MYSQL_PASSWORD% --execute="CREATE DATABASE IF NOT EXISTS %DATABASE_NAME%;"
if errorlevel 1 (
    echo Could not connect to MySQL. Check that MySQL is running and the credentials above are correct.
    pause
    exit /b 1
)

echo Starting RailConnect at http://localhost:8080 ...
cd /d "%~dp0.."
call mvn spring-boot:run
if errorlevel 1 pause
endlocal

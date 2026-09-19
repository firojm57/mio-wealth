@echo off
setlocal enabledelayedexpansion

echo ====================================================================
echo   Mio Wealth - Build UI, Embed Assets, and Start Spring Boot Server
echo ====================================================================
echo.

set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%"

:: 1. Verify Node.js and npm
where npm >nul 2>nul
if %ERRORLEVEL% neq 0 goto :err_node

:: 2. Build Angular Frontend (imui)
echo [1/3] Building Angular 21 UI...
cd /d "%ROOT_DIR%imui"
call npm run build
if %ERRORLEVEL% neq 0 goto :err_ng

:: 3. Copy compiled static files to Spring Boot static resources
echo.
echo [2/3] Embedding UI assets into Spring Boot static resources...
set "STATIC_DIR=%ROOT_DIR%server\src\main\resources\static"
if not exist "%STATIC_DIR%" mkdir "%STATIC_DIR%"

:: Clean existing static resources and copy fresh build
del /Q /S "%STATIC_DIR%\*" >nul 2>&1
xcopy /E /Y /I "%ROOT_DIR%imui\dist\imui\browser\*" "%STATIC_DIR%\" >nul
if %ERRORLEVEL% neq 0 goto :err_copy
echo [OK] Static assets successfully embedded.

:: 4. Start Spring Boot Server
echo.
echo [3/3] Starting Spring Boot Server on http://localhost:8080...
echo [TIP] Ensure PostgreSQL is running on port 5432 - e.g. via 'docker compose up -d postgres'
echo.

cd /d "%ROOT_DIR%server"
if exist "mvnw.cmd" (
    call mvnw.cmd spring-boot:run
) else (
    call mvn spring-boot:run
)

if %ERRORLEVEL% neq 0 goto :err_server

exit /b 0

:err_node
echo [ERROR] Node.js or npm is not installed or not in your system PATH.
echo Please install Node.js v20+ from https://nodejs.org/
pause
exit /b 1

:err_ng
echo [ERROR] Angular build failed. Please review the errors above.
pause
exit /b 1

:err_copy
echo [ERROR] Failed to copy static assets to server static resources.
pause
exit /b 1

:err_server
echo [ERROR] Spring Boot server stopped or encountered an error.
pause
exit /b 1

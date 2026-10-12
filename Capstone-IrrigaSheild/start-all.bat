@echo off
setlocal EnableDelayedExpansion

REM Detect a valid JDK if JAVA_HOME is not set or invalid in environment
if defined JAVA_HOME (
    if not exist "%JAVA_HOME%\bin\java.exe" set "JAVA_HOME="
)
if not defined JAVA_HOME (
    for /f "delims=" %%I in ('where javac 2^>nul') do (
        if not defined JAVA_HOME (
            for %%D in ("%%~dpI..") do (
                if exist "%%~fD\bin\java.exe" set "JAVA_HOME=%%~fD"
            )
        )
    )
)
if not defined JAVA_HOME (
    for /d %%D in ("%USERPROFILE%\.jdks\*" "C:\Program Files\Java\*" "C:\Program Files\Eclipse Adoptium\*" "C:\Program Files\Microsoft\*") do (
        if exist "%%~fD\bin\java.exe" set "JAVA_HOME=%%~fD"
    )
)
if not defined JAVA_HOME (
    for /d %%D in ("C:\Program Files\JetBrains\*" "%LOCALAPPDATA%\Programs\*") do (
        if exist "%%~fD\jbr\bin\java.exe" set "JAVA_HOME=%%~fD\jbr"
    )
)
if defined JAVA_HOME (
    set "PATH=!JAVA_HOME!\bin;!PATH!"
)

if /I "%~1"=="__start" goto START_SERVICE

echo ==========================================
echo   Starting all microservices in parallel
echo ==========================================
echo.

set "SERVICES=eureka-server api-gateway auth-service farm-service policy-service event-claims-service"
set "LOG_DIR=%~dp0runtime-logs"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo Starting services...
echo.

for %%S in (%SERVICES%) do (
    echo Starting %%S...
    start "Runtime - %%S" /b "%ComSpec%" /c call "%~f0" __start %%S
    REM Give Eureka server a head start to bind port 8761 before clients register
    if "%%S"=="eureka-server" (
        timeout /t 8 /nobreak >nul 2>&1
        if errorlevel 1 ping 127.0.0.1 -n 8 >nul
    )
)

echo.
echo ==========================================
echo   All services have been launched
echo ==========================================
echo.
echo Service Port Mapping:
echo   [8761] eureka-server
echo   [8080] api-gateway
echo   [8081] auth-service
echo   [8082] farm-service
echo   [8083] policy-service
echo   [8084] event-claims-service
echo.
echo Runtime logs:
echo %LOG_DIR%\
echo.

exit /b 0


:START_SERVICE

set "SERVICE=%~2"
set "JAR="

set "JAVA_CMD=java"
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

pushd "%~dp0%SERVICE%"

if not exist "target" (
    echo [ERROR] target directory not found for %SERVICE%
    popd
    exit /b 1
)

for /f "delims=" %%J in ('dir /b /a-d /o-d "target\*.jar" 2^>nul') do (
    echo %%J | findstr /i "\.original$" >nul
    if errorlevel 1 (
        if not defined JAR set "JAR=%%J"
    )
)

if not defined JAR (
    echo [ERROR] No JAR found for %SERVICE%
    echo No JAR found for %SERVICE% > "%~dp0runtime-logs\%SERVICE%.log"
    popd
    exit /b 1
)

echo [%SERVICE%] Using JAR: !JAR!
echo [%SERVICE%] Starting...

"!JAVA_CMD!" -Deureka.instance.ip-address=127.0.0.1 -Deureka.instance.prefer-ip-address=true -jar "target\!JAR!" > "%~dp0runtime-logs\%SERVICE%.log" 2>&1

popd
exit /b 0

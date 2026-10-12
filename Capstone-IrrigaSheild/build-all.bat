@echo off
setlocal EnableDelayedExpansion

REM Detect JAVA_HOME if not already set in environment
if not defined JAVA_HOME (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr" (
        set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr"
        set "PATH=!JAVA_HOME!\bin;!PATH!"
    )
)

REM ==============================================================================
REM IrrigaShield Parallel Microservices Build Script
REM 
REM NOTE: You do NOT need to manually delete existing JAR files before building.
REM The "mvn clean package -DskipTests" command automatically invokes Maven's
REM clean lifecycle:
REM   clean      -> Deletes existing <service>/target/ directory completely
REM   compile    -> Recompiles fresh source files
REM   package    -> Generates fresh, non-reused Spring Boot executable JARs
REM
REM The log cleanup below only resets the build-logs directory (*.log, *.status).
REM ==============================================================================

if /I "%~1"=="__build" goto BUILD_SERVICE

echo =======================================================
echo   Packaging all IrrigaShield microservices in parallel
echo =======================================================
echo.

set "SERVICES=eureka-server api-gateway auth-service farm-service policy-service event-claims-service"
set "LOG_DIR=%~dp0build-logs"

REM Create build-logs directory if it does not exist
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM Clear previous execution logs and status markers (does NOT touch target/*.jar)
echo Cleaning previous build logs...
del /q "%LOG_DIR%\*.log" 2>nul
del /q "%LOG_DIR%\.build-*.status" 2>nul

echo.

REM Start all builds in parallel background tasks
for %%S in (%SERVICES%) do (
    echo Starting clean package: %%S
    start "Build - %%S" /b "%ComSpec%" /c call "%~f0" __build %%S
)

echo.
echo =======================================================
echo   Waiting for all parallel builds to complete...
echo =======================================================
echo.

:WAIT_LOOP

set "DONE=1"

for %%S in (%SERVICES%) do (
    if not exist "%LOG_DIR%\.build-%%S.status" (
        set "DONE=0"
    )
)

if "!DONE!"=="0" (
    timeout /t 2 /nobreak >nul 2>&1
    if errorlevel 1 ping 127.0.0.1 -n 3 >nul
    goto WAIT_LOOP
)

echo.
echo =======================================================
echo   Build Results
echo =======================================================
echo.

set "FAILED=0"

for %%S in (%SERVICES%) do (
    set "STATUS="
    set /p STATUS=<"%LOG_DIR%\.build-%%S.status"

    if "!STATUS!"=="SUCCESS" (
        echo [SUCCESS] %%S
    ) else (
        echo [FAILED ] %%S
        echo           Log: %LOG_DIR%\%%S.log
        set "FAILED=1"
    )
)

echo.

if "!FAILED!"=="1" (
    echo =======================================================
    echo   One or more services failed to build
    echo =======================================================
    echo.
    echo Detailed failure logs preserved in:
    echo %LOG_DIR%\
    echo.
    pause
    exit /b 1
)

REM When all services succeed, clean up temporary build logs and status flags
del /q "%LOG_DIR%\*.log" 2>nul
del /q "%LOG_DIR%\.build-*.status" 2>nul

echo =======================================================
echo   All microservices cleanly built and packaged!
echo =======================================================
echo.
echo Fresh JAR files are ready in each service's target/ folder.
echo You can now launch them using start-all.bat.
echo.

pause
exit /b 0


:BUILD_SERVICE

set "SERVICE=%~2"

REM Detect JAVA_HOME if not already set in environment
if not defined JAVA_HOME (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr" (
        set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr"
        set "PATH=!JAVA_HOME!\bin;!PATH!"
    )
)

echo [%SERVICE%] Running clean package...

pushd "%~dp0%SERVICE%"

REM "clean" deletes target/ automatically; "package" outputs fresh JARs
if exist "mvnw.cmd" (
    call ".\mvnw.cmd" clean package -DskipTests > "%~dp0build-logs\%SERVICE%.log" 2>&1
) else if exist "%~dp0%SERVICE%\mvnw.cmd" (
    call "%~dp0%SERVICE%\mvnw.cmd" clean package -DskipTests > "%~dp0build-logs\%SERVICE%.log" 2>&1
) else (
    call mvn clean package -DskipTests > "%~dp0build-logs\%SERVICE%.log" 2>&1
)

if errorlevel 1 (
    echo FAILED> "%~dp0build-logs\.build-%SERVICE%.status"
) else (
    echo SUCCESS> "%~dp0build-logs\.build-%SERVICE%.status"
)

popd
exit /b

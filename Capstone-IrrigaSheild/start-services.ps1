$javaHome = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr"
$javaExe = "$javaHome\bin\java.exe"
$env:JAVA_HOME = $javaHome
$env:PATH = "$javaHome\bin;$env:PATH"

$services = @("eureka-server", "api-gateway", "auth-service", "farm-service", "policy-service", "event-claims-service")
$logDir = "D:\Capstone\Capstone-IrrigaSheild\runtime-logs"
if (!(Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir | Out-Null }

foreach ($svc in $services) {
    $svcDir = "D:\Capstone\Capstone-IrrigaSheild\$svc"
    $jar = Get-ChildItem -Path "$svcDir\target\*.jar" | Where-Object { $_.Name -notmatch '\.original$' } | Select-Object -First 1
    if ($jar) {
        Write-Host "Starting $svc with $($jar.Name)..."
        $logFile = "$logDir\$svc.log"
        $errFile = "$logDir\$svc-err.log"
        $svcArgs = @("-Deureka.instance.ip-address=127.0.0.1", "-Deureka.instance.prefer-ip-address=true", "-jar", $jar.FullName)
        Start-Process -FilePath $javaExe -ArgumentList $svcArgs -WorkingDirectory $svcDir -RedirectStandardOutput $logFile -RedirectStandardError $errFile
        if ($svc -eq "eureka-server") {
            Write-Host "Waiting 8 seconds for Eureka server to initialize..."
            Start-Sleep -Seconds 8
        }
    } else {
        Write-Error "No JAR found for $svc in $svcDir\target"
    }
}

Write-Host "All services launched successfully in background."

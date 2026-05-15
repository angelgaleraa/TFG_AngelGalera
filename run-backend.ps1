$profile = if ($args.Count -gt 0) { $args[0] } elseif ($env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE } else { "cloud" }

if ($profile -eq "cloud") {
    if (Test-Path ".\.secrets\cloud-db.env.ps1") {
        . ".\.secrets\cloud-db.env.ps1"
    } elseif (Test-Path ".\cloud-db.env.ps1") {
        . ".\cloud-db.env.ps1"
    }

    if ([string]::IsNullOrWhiteSpace($env:APP_DB_URL) -or
        [string]::IsNullOrWhiteSpace($env:APP_DB_USERNAME) -or
        [string]::IsNullOrWhiteSpace($env:APP_DB_PASSWORD)) {
        Write-Error "Faltan credenciales cloud. Revisa .secrets\cloud-db.env.ps1 o ejecuta .\run-backend.ps1 dev para usar MySQL local."
        exit 1
    }
}

$jdkCandidates = @(
    $env:JAVA_HOME,
    "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot",
    "C:\Program Files\Java\jdk-23",
    "C:\Program Files\Java\jdk-21"
) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }

$javaHome = $jdkCandidates | Where-Object { Test-Path (Join-Path $_ "bin\java.exe") } | Select-Object -First 1
if (-not $javaHome) {
    Write-Error "No se encontro un JDK compatible. Instala Java 21+ y vuelve a intentarlo."
    exit 1
}

$env:JAVA_HOME = $javaHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:SPRING_PROFILES_ACTIVE = $profile

Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "SPRING_PROFILES_ACTIVE=$env:SPRING_PROFILES_ACTIVE"
Write-Host "Arrancando backend en http://localhost:8081 ..."
& .\mvnw.cmd -f .\backend\pom.xml spring-boot:run

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

Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "Arrancando frontend JavaFX ..."
& .\mvnw.cmd -f .\frontend\pom.xml javafx:run

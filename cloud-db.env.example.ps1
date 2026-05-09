# Copia este archivo como cloud-db.env.ps1 y rellena los valores reales de Aiven.
# No subas cloud-db.env.ps1 al repositorio.

$env:LOCAL_DB_HOST = "localhost"
$env:LOCAL_DB_PORT = "3306"
$env:LOCAL_DB_NAME = "rental_platform"
$env:LOCAL_DB_USERNAME = "root"
$env:LOCAL_DB_PASSWORD = "root"

$env:CLOUD_DB_HOST = "tu-host.aivencloud.com"
$env:CLOUD_DB_PORT = "12345"
$env:CLOUD_DB_NAME = "defaultdb"
$env:CLOUD_DB_USERNAME = "avnadmin"
$env:CLOUD_DB_PASSWORD = "pega-aqui-la-password"

$env:APP_DB_URL = "jdbc:mysql://${env:CLOUD_DB_HOST}:${env:CLOUD_DB_PORT}/${env:CLOUD_DB_NAME}?sslMode=REQUIRED&serverTimezone=UTC"
$env:APP_DB_USERNAME = $env:CLOUD_DB_USERNAME
$env:APP_DB_PASSWORD = $env:CLOUD_DB_PASSWORD
$env:SPRING_PROFILES_ACTIVE = "cloud"

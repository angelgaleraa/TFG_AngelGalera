# Credenciales locales/cloud para la BD. Archivo ignorado por git.

$env:LOCAL_DB_HOST = "localhost"
$env:LOCAL_DB_PORT = "3306"
$env:LOCAL_DB_NAME = "rental_platform"
$env:LOCAL_DB_USERNAME = "root"
$env:LOCAL_DB_PASSWORD = "root"

$env:CLOUD_DB_HOST = "rental-platform-mysql-angelgs230-7ae8.c.aivencloud.com"
$env:CLOUD_DB_PORT = "26948"
$env:CLOUD_DB_NAME = "defaultdb"
$env:CLOUD_DB_USERNAME = "avnadmin"
$env:CLOUD_DB_PASSWORD = "AVNS_GcDRtl28GsHqaNvzBB1"

$env:APP_DB_URL = "jdbc:mysql://${env:CLOUD_DB_HOST}:${env:CLOUD_DB_PORT}/${env:CLOUD_DB_NAME}?sslMode=REQUIRED&serverTimezone=UTC"
$env:APP_DB_USERNAME = $env:CLOUD_DB_USERNAME
$env:APP_DB_PASSWORD = $env:CLOUD_DB_PASSWORD
$env:SPRING_PROFILES_ACTIVE = "cloud"

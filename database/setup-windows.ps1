param(
    [string]$MySqlHost = "127.0.0.1",
    [int]$MySqlPort = 3306,
    [string]$RootUser = "root",
    [string]$AppUser = "railconnect",
    [string]$AppPassword = "railconnect_dev"
)

$ErrorActionPreference = "Stop"

$mysqlCommand = Get-Command mysql.exe -ErrorAction SilentlyContinue
if ($null -eq $mysqlCommand) {
    $commonPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    if (Test-Path $commonPath) {
        $mysqlPath = $commonPath
    } else {
        throw "mysql.exe was not found. Add the MySQL Server bin folder to PATH and run this script again."
    }
} else {
    $mysqlPath = $mysqlCommand.Source
}

$escapedPassword = $AppPassword.Replace("'", "''")
$sql = @"
CREATE DATABASE IF NOT EXISTS railconnect
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$AppUser'@'localhost' IDENTIFIED BY '$escapedPassword';
ALTER USER '$AppUser'@'localhost' IDENTIFIED BY '$escapedPassword';
GRANT ALL PRIVILEGES ON railconnect.* TO '$AppUser'@'localhost';
CREATE USER IF NOT EXISTS '$AppUser'@'127.0.0.1' IDENTIFIED BY '$escapedPassword';
ALTER USER '$AppUser'@'127.0.0.1' IDENTIFIED BY '$escapedPassword';
GRANT ALL PRIVILEGES ON railconnect.* TO '$AppUser'@'127.0.0.1';
FLUSH PRIVILEGES;
"@

Write-Host "Configuring MySQL database 'railconnect' on $MySqlHost`:$MySqlPort..."
Write-Host "Enter the MySQL root password when prompted."
& $mysqlPath --host $MySqlHost --port $MySqlPort --user $RootUser --password --execute $sql
if ($LASTEXITCODE -ne 0) {
    throw "MySQL setup failed. Check that MySQL Server is running and the root credentials are correct."
}

Write-Host "Database and application user are ready. Existing data was not dropped."
Write-Host "Run these commands in the same PowerShell window before starting Spring Boot:"
Write-Host '$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/railconnect?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Colombo"'
Write-Host "`$env:DB_USERNAME = `"$AppUser`""
Write-Host '$env:DB_PASSWORD = "<the AppPassword value used above>"'
Write-Host "mvn spring-boot:run"
Write-Host "Flyway will apply the repository migrations and seed the demo data automatically."

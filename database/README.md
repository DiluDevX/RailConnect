# Database setup

## Simple local setup (Vehicle Rental-style)

RailConnect supports the same straightforward local setup as the team's
Vehicle Rental project. Install MySQL Server, start the MySQL service, and use:

- host: `localhost`
- port: `3306`
- database: `railconnect`
- username: `root`
- password: `root`

On Windows, double-click [`start-windows.bat`](start-windows.bat). It creates
the database if needed and starts Spring Boot. Alternatively, run
[`setup-mysql.sql`](setup-mysql.sql) in MySQL Workbench, then run
`mvn spring-boot:run` from the project root.

On macOS, make the launcher executable once with `chmod +x
database/start-macos.command`, then double-click [`start-macos.command`](start-macos.command)
in Finder. It detects the common Homebrew MySQL locations, creates the database,
and starts Spring Boot.

Hibernate uses `spring.jpa.hibernate.ddl-auto=update`, so it creates or updates
tables automatically when the application starts. If your MySQL password is
different, edit `src/main/resources/application.properties` or set
`DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` before starting the app.

Flyway runs after the database is created and automatically applies the schema
and demo-data migrations. This seeds the demo users, trains, routes, carriages,
seats, schedules, bookings, payments, and complaints on a fresh database.
Hibernate remains in `update` mode to match the simple Vehicle Rental setup.

The canonical MySQL schema and demo data are versioned Flyway migrations in:

- `src/main/resources/db/migration/V1__create_railconnect_schema.sql`
- `src/main/resources/db/migration/V2__seed_demo_data.sql`
- `src/main/resources/db/migration/V3__correct_demo_password_hash.sql`

The recommended setup is `docker compose up -d db` followed by
`mvn spring-boot:run`. Flyway applies missing versions automatically and records them
in `flyway_schema_history`.

## Windows with local MySQL Server

SQL Server Management Studio is not used by this project. The application is
configured for **MySQL 8**. On Windows, install MySQL Community Server 8.0 and
add its `bin` folder to `PATH`, or run the helper from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\database\setup-windows.ps1
```

The helper creates the `railconnect` database and the `railconnect` application
user. It does not drop tables or delete the existing MySQL data. Start the app
with the local MySQL port:

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/railconnect?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Colombo"
$env:DB_USERNAME = "railconnect"
$env:DB_PASSWORD = "railconnect_dev"
mvn spring-boot:run
```

On startup, Flyway automatically executes `V1` through the latest migration and
seeds the development data. If the schema already exists, only unapplied
migrations are executed; do not edit a migration that is recorded in
`flyway_schema_history`.

### Back up or restore an existing MySQL database

These commands are optional and are intended for a dump created from another
RailConnect MySQL database. They use the standard MySQL client, not SSMS:

```powershell
# Run from the repository root; you will be prompted for the database password.
cmd /c "mysqldump -h 127.0.0.1 -P 3306 -u railconnect -p railconnect > database\railconnect-backup.sql"

# Restore only when you intentionally want to import that dump.
cmd /c "mysql -h 127.0.0.1 -P 3306 -u railconnect -p railconnect < database\railconnect-backup.sql"
```

Do not restore a dump over a database that is currently being migrated by the
running application. Stop Spring Boot first, keep a backup, and only restore a
dump produced from the same schema version. For a fresh setup, use Flyway rather
than manually importing the migration files.

If importing manually from the repository root with the MySQL command-line client:

```sql
SOURCE src/main/resources/db/migration/V1__create_railconnect_schema.sql;
SOURCE src/main/resources/db/migration/V2__seed_demo_data.sql;
SOURCE src/main/resources/db/migration/V3__correct_demo_password_hash.sql;
```

Do not edit an applied migration. Add `V4__description.sql`, then V5, and so on.
This preserves repeatable database history for all six members.

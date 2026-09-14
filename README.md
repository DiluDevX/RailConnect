# RailConnect

RailConnect is a Java web application for train administration, timetable planning,
seat inventory, ticket booking and passenger complaints. It is intentionally scoped
as a university MVP: the booking workflow is complete, but payment and notifications
are simulated so the group can explain and defend the backend code.

## Technology

- Java 17 and Spring Boot 3
- Spring MVC + Thymeleaf server-rendered UI
- Spring Data JPA and Hibernate
- Spring Security with BCrypt passwords and role-based access
- MySQL 8 in Docker
- Flyway versioned SQL migrations
- JUnit 5, AssertJ and H2 for automated integration tests

The UI follows the supplied Stitch references: navy `#001F3F`, blue accents,
light-grey application background, Inter/system typography, clear cards, tables and
seat controls. No Stitch runtime or external frontend framework is required.

## Quick start

Requirements: Java 17+, Maven 3.9+, Docker Desktop.

```bash
docker compose up -d db
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

MySQL is exposed at `localhost:3307`; Flyway creates and seeds the schema on first
startup. To stop the application, press `Ctrl+C`. To stop MySQL:

```bash
docker compose down
```

Do not use `docker compose down -v` unless you intentionally want to delete the
local database volume.

## Windows setup with local MySQL (without Docker)

If Docker Desktop is not being used, install **MySQL Community Server 8.0** on
Windows and make sure `mysql.exe` is available in PowerShell. The default MySQL
installation path is usually:

```text
C:\Program Files\MySQL\MySQL Server 8.0\bin
```

From the project root, double-click the one-step launcher:

`database\start-windows.bat`

It creates the `railconnect` database if needed and starts the application. This
matches the simple Vehicle Rental setup: MySQL `root/root` on port `3306`.
If your password differs, edit the variables at the top of the batch file.
You can also run the commands manually:

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/railconnect?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"
mvn spring-boot:run
```

Flyway creates the tables and seeds the demo data automatically on first
startup, while Hibernate remains in `update` mode for local development.

The full Windows checklist, backup command and optional SQL-dump restore command
are in [database/README.md](database/README.md).

### macOS one-click setup

On macOS, install and start MySQL, run `chmod +x database/start-macos.command`
once, then double-click that file in Finder. It creates the `railconnect`
database and starts the application. The defaults are the same as Windows:
MySQL `root/root` on port `3306`.

## Demo accounts

All seeded demo accounts use the password `password`.

| Role | Email | Main access |
|---|---|---|
| Railway administrator | `admin@railconnect.lk` | Operational administration; no customer checkout |
| Booking officer | `officer@railconnect.lk` | Assisted customer booking with cash, booking verification and complaints |
| Passenger | `passenger@railconnect.lk` | Search, booking and complaints |

These are development credentials only. Never reuse them in a deployed system.

The development database includes six trains, multiple carriage classes and
current/future demo schedules so the search and seat-selection flows have useful
variety immediately after startup.

## Six member modules

| Member | Major function | Package |
|---|---|---|
| Rukshana | Train Management | `features/train` |
| Kenula | Route Management | `features/route` |
| Oneli | Train Schedule Management | `features/schedule` |
| Kaveen | Train Carriage and Seat Management | `features/carriage` |
| Dilum | Ticket Booking Management | `features/booking` |
| Srither | Complaint Management | `features/complaint` |

Each package contains its own `domain`, `dto`, `repository`, `service` and `web`
folders. This is a modular monolith, not six separate applications. That is the
right scope for this assignment: members can own clear modules without adding the
deployment and data-consistency complexity of microservices.

See [docs/TEAM-OWNERSHIP.md](docs/TEAM-OWNERSHIP.md) for boundaries and viva topics.
The shared vocabulary is defined in [CONTEXT.md](CONTEXT.md), and the exact mapping
between the lecture EER, MySQL tables and application screens is documented in
[docs/DATA-MODEL-MAPPING.md](docs/DATA-MODEL-MAPPING.md).

## Important business rules

- A train's capacity is derived from its active carriages; it is not stored twice.
- Routes cannot use the same start and end station.
- One train cannot have overlapping operating schedules on the same date.
- Creating a carriage generates its physical seats once; capacity is then fixed.
- A physical seat may be active while being unavailable for one particular schedule.
- Booking totals are calculated by the backend, never accepted from the browser.
- A seat is temporarily held before payment.
- Passengers use a fake Visa checkout; booking officers record cash collected at the counter.
- Assisted officer bookings ask only for the customer name (defaulting to `Customer`); contact fields remain internal and are not shown on the ticket view.
- Card details are format-validated for the demo and are never persisted.
- Railway administrators cannot enter customer search or checkout flows.
- Failed payment releases the hold; retry reuses the same booking record.
- Successful payment confirms the booking and reservation together.
- Cancellation releases seats and records a simulated refund when appropriate.
- Search filters out schedules that do not have enough active seats for the requested passenger count.
- Search supports up to 10 passengers and refreshes matching schedules as route, date or passenger inputs change.
- Each train can define first-, second- and third-class fares; the seat map shows the configured fare per carriage.
- Confirmed bookings can be printed from the booking details page using the browser print dialog.
- Booking officers and passengers use the same simple print action for the confirmed ticket.
- A guest who selects a schedule is returned to that schedule's seat map after signing in; protected booking and admin routes enforce their role conditions.
- Shared success/error notifications slide in from the upper-right and station fields provide full-width filtered suggestions.
- Passengers can change their password in Account settings and use the local demo forgot-password flow.
- Passenger complaint access is ownership-checked; staff can review and respond.

## Tests and build

```bash
mvn clean test
mvn clean package
docker compose ps
```

Automated tests use an isolated H2 database. The application run uses real MySQL 8.
The tested flows and remaining manual checks are documented in
[docs/TESTING.md](docs/TESTING.md).

## Configuration

Defaults work with `docker-compose.yml`. Override them when needed:

```bash
export DB_URL='jdbc:mysql://localhost:3307/railconnect?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Colombo'
export DB_USERNAME='railconnect'
export DB_PASSWORD='railconnect_dev'
export SERVER_PORT='8080'
mvn spring-boot:run
```

`src/main/resources/application-example.properties` lists the available settings.

## Project limits

This is a demonstrable MVP, not a production railway system. It deliberately has:

- simulated Visa and cash payments with no card data or real gateway;
- optional local notification abstraction with no email provider;
- local forgot-password reset links are displayed in the UI for demonstration instead of being emailed;
- a single application instance and one MySQL database;
- no live railway API, GPS tracking, dynamic pricing or online check-in;
- no production deployment, monitoring, backups or penetration testing.

Those omissions keep the implementation aligned with the proposal and the team's
current learning level. They should be described honestly in the report.

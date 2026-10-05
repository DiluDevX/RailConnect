# Testing evidence and checklist

## Automated integration tests

Run:

```bash
mvn clean test
```

Coverage focuses on high-risk backend rules:

- Spring application context loads.
- Documented demo password matches its BCrypt hash.
- Route rejects identical start and end stations.
- Schedule rejects an overlapping timetable for the same train.
- Successful simulated payment confirms the original pending booking.
- Failed payment releases seats and retry reuses the booking.
- Train create/read/update/deactivate lifecycle.
- Carriage creation generates seats and contributes derived train capacity.
- Complaint create/update/respond lifecycle.

## MySQL and browser verification

Start the real environment:

```bash
docker compose up -d db
mvn spring-boot:run
```

The following flows were exercised against MySQL 8:

- public home and exact-date train search;
- passenger login;
- seat map with confirmed/out-of-service seats unavailable;
- booking hold, failed payment, released reservation, retry and success;
- booking details/e-ticket view;
- direct PDF e-ticket download and one-page PDF rendering;
- current-day search with live route/date/passenger filtering (up to 10 passengers) and availability filtering;
- account profile update, password change and local forgot-password reset link;
- dark-mode rendering, outside-click account menu close and responsive seat-map layout;
- passenger complaint creation;
- railway administrator dashboard;
- train create, update and deactivate;
- train, route, schedule, carriage/seat, booking and complaint screens;
- staff complaint response and status update;
- booking officer denied train administration but allowed booking administration.

Temporary QA records should be removed after manual testing. Never use live passenger
or card data: payment is simulated and no card fields exist.

## Before a demonstration

```bash
docker compose ps
mvn test
curl -I http://localhost:8080/
```

The home search form defaults to the current date. Migrations V6 and V7 seed several
current-day and future demo schedules across multiple trains and routes, while later
schedules remain available for date-specific testing.

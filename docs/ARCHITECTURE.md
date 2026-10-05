# Architecture and OOP guide

RailConnect is a layered modular monolith:

```text
Browser -> Controller -> Service -> Repository -> MySQL
                       -> NotificationService (optional local implementation)
```

## Layer responsibilities

- Controllers handle URLs, form binding, validation results and redirects.
- Services implement business rules and transaction boundaries.
- Repositories contain persistence queries.
- Entities model state and protect state transitions through methods.
- DTO/form classes carry untrusted browser input; entities are not bound directly.
- Thymeleaf templates render the server-side view.

## OOP concepts visible in the code

- Encapsulation: fields are private and state changes use methods such as
  `confirm`, `release`, `cancel`, `respond` and `update`.
- Abstraction: controllers call services instead of database code.
- Interface and polymorphism: `NotificationService` is an interface and
  `LocalNotificationService` is its current implementation.
- Composition/association: a schedule references one train and route; a booking
  contains selected-seat rows and reservation/payment records.
- Dependency injection: Spring supplies repository and service dependencies through
  constructors.

Inheritance is intentionally limited to `BaseEntity` for identifiers and audit
timestamps. Forcing inheritance into every module would make the model harder to
explain without solving a real problem.

## Transaction-sensitive booking sequence

1. The passenger selects active, unreserved seats for an active schedule.
2. `BookingService` locks each schedule/seat reservation key.
3. The backend calculates fares and creates one pending booking plus held reservations.
4. Simulated success confirms the same booking and its reservations.
5. Simulated failure marks the attempt failed and releases reservations.
6. Retry reuses the failed booking and tries to hold the same seats again.
7. Cancellation releases seats and changes a successful payment to refunded.

The database uniqueness constraint on `(schedule_id, seat_id)` is the final protection
against double-booking. The service also uses a pessimistic lock so the error can be
handled as a business rule.

## Why this is not microservices

Six separately deployed services would require API versioning, distributed
transactions, service discovery, more Docker containers and failure recovery.
That complexity would distract from the assessed Java/OOP/database work. Feature
packages give clear ownership while preserving one reliable transaction boundary.

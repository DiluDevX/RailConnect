# Team ownership and module boundaries

The folders are ownership boundaries for collaboration, not permission to change
shared contracts without discussing the impact. Database and shared security changes
can affect every member.

## Rukshana — Train Management

Owns `features/train` and the `trains` table.

- Create, list/search, update and deactivate/reactivate trains.
- Explain why `totalCapacity` is derived from active carriages.
- Coordinate with Kaveen when changing train/carriage relationships.

## Kenula — Route Management

Owns `features/route` and the `routes` table.

- Create, list/search, update and deactivate/reactivate routes.
- Validate different start/end stations and positive distance.
- Coordinate with Oneli because schedules reference routes.

## Oneli — Train Schedule Management

Owns `features/schedule` and the `train_schedules` table.

- Create, view/search, update and cancel schedules.
- Validate active train/route, future date, arrival after departure and no overlap.
- Preserve existing bookings when cancelling a schedule; do not hard-delete history.

## Kaveen — Carriage and Seat Management

Owns `features/carriage`, `carriages` and `seats`.

- Create, view, update and activate/deactivate carriages.
- Generate seats from capacity and manage physical seat availability.
- Explain the difference between physical seat status and per-schedule reservation.

## Dilum — Ticket Booking Management

Owns `features/booking`, `ticket_bookings`, `booking_seats`, `seat_reservations`
and `payments`.

- Search hand-off, seat selection, temporary hold, backend total, simulated payment,
  retry, confirmation, e-ticket, cancellation and simulated refund.
- Protect ownership and prevent duplicate seat reservations.
- Keep `BookingStatus`, `PaymentStatus` and `ReservationStatus` separate.

## Srither — Complaint Management

Owns `features/complaint` and `complaints`.

- Passenger create/view/update/withdraw and staff list/respond/status update.
- Complaint may be general or linked to an accessible booking.
- Only open complaints can be edited or withdrawn by passengers.

## Shared code

- `auth`: registration, login, roles and BCrypt. User management remains a supporting
  function, not a seventh major module.
- `shared/security`: authorization rules and current-user lookup.
- `shared/exception`: user-safe error handling.
- `templates/fragments`, CSS and JavaScript: common UI. Preserve the design tokens.
- `db/migration`: shared schema contract. Never edit a migration already used by
  another member; add the next version instead.

## Git collaboration rule

Use one feature branch per task, keep commits small, and pull/rebase before handing
work to the integrator. Do not copy a whole member folder over another member's newer
version. Resolve changes file-by-file and run `mvn test` before sharing.

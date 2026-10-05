# EER, database and interface mapping

This document states how the conceptual RailConnect EER model is represented by the current implementation. The major entities are covered, but the implementation is intentionally more precise in a few places than the original diagram.

| EER concept | Database representation | Main interface | Relationship represented |
| --- | --- | --- | --- |
| USER | `users` | Registration, login and authenticated header | A user makes bookings and submits complaints |
| TRAIN | `trains` | Train Management | A train has carriages and operates schedules |
| ROUTE | `routes` | Route Management | A route is used by schedules |
| TRAIN_SCHEDULE | `train_schedules` | Schedule Management and journey search | Each schedule references one train and one route |
| CARRIAGE | `carriages` | Carriage Inventory | Each carriage belongs to one train |
| SEAT | `seats` | Physical Seat Status and booking seat map | Each seat belongs to one carriage |
| TICKET_BOOKING | `ticket_bookings` | Booking Management, My Bookings and e-ticket | Each booking belongs to one user and one schedule |
| PAYMENT | `payments` | Simulated Visa or counter-cash payment and attempt history | Each payment attempt belongs to one booking and records its method |
| COMPLAINT | `complaints` | Complaint Management and My Complaints | Each complaint belongs to one user and may reference one booking |

## Associations made explicit by the implementation

`booking_seats` is an associative record between `ticket_bookings` and `seats`. It records every seat sold under a booking and the trusted fare used for that seat.

`seat_reservations` applies seat occupancy to a specific schedule. The unique `(schedule_id, seat_id)` constraint prevents two active booking records from claiming the same seat for the same journey. This table is necessary because physical seat status alone cannot represent journey-specific availability.

`payments` allows more than one attempt for a booking. This preserves failed-attempt history and makes the retry workflow explainable. The logical cardinality is therefore `TICKET_BOOKING 1 — 0..N PAYMENT`, not one-to-one.

The `method` attribute differentiates `SIMULATED_CARD` from `CASH`. Passenger checkout accepts only the simulated-card method; booking officers accept cash on behalf of a customer. Raw card details are not part of the EER or database because RailConnect does not persist them.

## Intentional EER-to-code differences

### User specialization

The conceptual EER diagram uses disjoint subtypes such as Passenger, Booking Officer and Administrator. The MVP stores those categories as the `role` attribute of one `users` record. This is a single-table role model: simpler for login and authorization, but not an exact table-per-subtype implementation of the EER triangle.

For documentation consistency, either label the EER specialization as a conceptual access-role classification or replace it with a `Role` attribute. Adding empty subtype tables only to imitate the drawing would add complexity without storing subtype-specific data.

### Payment cardinality

If the submitted EER currently shows one payment per booking, it should be updated to show zero-to-many payment attempts per booking. The interface exposes this as a payment-attempt history on Booking Details.

### Derived capacity

Train capacity is derived from active carriage capacities. It should remain a derived attribute in the EER and must not be duplicated as a writable column on `trains`.

## Six-member scope coverage

The six assessed modules are represented independently in source packages and UI areas: Train, Route, Train Schedule, Carriage and Seat, Ticket Booking, and Complaint Management. Registration/login is shared supporting functionality rather than a seventh major module.

# RailConnect Domain Context

RailConnect is a web-based train scheduling and ticket-booking system. It manages railway inventory, scheduled journeys, passenger bookings and complaints. Real payment and email providers are outside the project scope; those interactions are simulated or optional.

## People and access

**User** — A person with an authenticated RailConnect account and one assigned role.

**Passenger** — A user who searches schedules, books seats, views tickets and submits complaints.

**Booking Officer** — A staff user who verifies bookings and assists with passenger-facing records.

**Railway Administrator** — A staff user who manages trains, routes, schedules, carriages and seats.

## Railway inventory

**Train** — A named railway vehicle. Its usable passenger capacity is derived from its active carriages rather than stored independently.

**Carriage** — A numbered physical coach belonging to one train. It has a travel class, a capacity and a collection of seats.

**Seat** — A numbered place within one carriage. Physical seat status says whether it is usable; it does not say whether the seat is free for a particular journey.

**Route** — A reusable definition of travel from one start station to one end station over a stated distance.

**Train Schedule** — One dated journey that assigns one train to one route with departure, arrival and base-fare information.

## Booking and support

**Ticket Booking** — A passenger's request for one scheduled journey and one or more seats. It owns the booking reference, trusted total and booking lifecycle.

**Booking Seat** — The recorded link between a booking and each selected physical seat, including the fare charged for that seat.

**Seat Reservation** — The per-schedule claim on a physical seat. A reservation may be held temporarily, confirmed after payment, or released after failure or cancellation.

**Payment Attempt** — One simulated attempt to pay for a booking. A booking can have multiple attempts, but each attempt belongs to exactly one booking.

**Complaint** — A passenger support request. It belongs to one user and may optionally reference one of that user's bookings.

## Important distinctions

- A **Train** owns physical **Carriages**; a **Train Schedule** uses the train for a dated journey.
- A **Seat** can exist and be active while already reserved for one particular schedule.
- A **Ticket Booking** records the commercial transaction; a **Seat Reservation** prevents double-booking.
- A **Payment Attempt** records simulated payment history; payment success changes booking and reservation state.
- Deactivation and cancellation preserve history. They do not hard-delete records that other modules may reference.

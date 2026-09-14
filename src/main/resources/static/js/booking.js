document.addEventListener("DOMContentLoaded", () => {
    const seats = Array.from(document.querySelectorAll('input[name="seatIds"]'));
    const count = document.getElementById("selected-count");
    const selectedSeats = document.getElementById("selected-seats");
    const classFilter = document.getElementById("class-filter");
    const bookingForm = seats[0]?.closest("form");
    const baseFare = Number(bookingForm?.dataset.baseFare || 0);
    const seatFareTotal = document.getElementById("seat-fare-total");
    const reservationFee = document.getElementById("reservation-fee");
    const estimatedTotal = document.getElementById("estimated-total");
    const continueButton = document.getElementById("continue-booking");
    const classFares = {};
    document.querySelectorAll(".carriage-deck[data-carriage-class]").forEach(deck => {
        classFares[deck.dataset.carriageClass] = Number(deck.dataset.fare || 0);
    });
    const money = amount => `LKR ${amount.toFixed(2)}`;

    const refresh = () => {
        const selected = seats.filter(seat => seat.checked);
        if (count) count.textContent = String(selected.length);
        if (selectedSeats) {
            selectedSeats.textContent = selected.length
                ? selected.map(seat => seat.dataset.seatLabel || seat.value).join(", ")
                : "Choose a seat";
        }
        const fare = selected.reduce((total, seat) => {
            const seatClass = seat.closest(".seat-choice")?.dataset.carriageClass;
            return total + (classFares[seatClass] || baseFare);
        }, 0);
        const fee = selected.length ? 100 : 0;
        if (seatFareTotal) seatFareTotal.textContent = money(fare);
        if (reservationFee) reservationFee.textContent = money(fee);
        if (estimatedTotal) estimatedTotal.textContent = money(fare + fee);
        if (continueButton) continueButton.disabled = selected.length === 0;
    };

    if (classFilter) {
        classFilter.addEventListener("change", () => {
            document.querySelectorAll(".carriage-deck[data-carriage-class]").forEach(deck => {
                deck.hidden = classFilter.value !== "ALL" && deck.dataset.carriageClass !== classFilter.value;
            });
        });
    }
    seats.forEach(seat => seat.addEventListener("change", refresh));
    refresh();
});

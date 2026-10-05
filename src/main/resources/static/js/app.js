document.addEventListener("DOMContentLoaded", () => {
    const toastStack = document.querySelector(".toast-stack") || (() => {
        const stack = document.createElement("div");
        stack.className = "toast-stack";
        stack.setAttribute("aria-live", "polite");
        document.body.appendChild(stack);
        return stack;
    })();
    const dismiss = toast => {
        if (!toast || toast.classList.contains("is-leaving")) return;
        toast.classList.add("is-leaving");
        window.setTimeout(() => toast.remove(), 240);
    };
    const showToast = (message, kind = "info", actions = null) => {
        const toast = document.createElement("div");
        toast.className = `toast toast-${kind}`;
        const text = document.createElement("p");
        text.textContent = message;
        toast.appendChild(text);
        if (actions) toast.appendChild(actions);
        toastStack.appendChild(toast);
        if (!actions) window.setTimeout(() => dismiss(toast), 5000);
        return toast;
    };
    document.querySelectorAll(".form-errors").forEach(error => {
        const message = error.textContent.trim().replace(/\s+/g, " ");
        if (message) showToast(message, "error");
        error.setAttribute("role", "alert");
    });
    document.querySelectorAll(".flash-stack .alert").forEach(alert => window.setTimeout(() => dismiss(alert), 5000));
    const themeButtons = Array.from(document.querySelectorAll("[data-theme-toggle]"));
    const applyTheme = theme => {
        document.documentElement.dataset.theme = theme;
        try { localStorage.setItem("railconnect-theme", theme); } catch (ignored) { }
        themeButtons.forEach(button => {
            const dark = theme === "dark";
            const icon = button.querySelector(".material-symbols-outlined");
            const label = button.querySelector("[data-theme-label]");
            if (icon) icon.textContent = dark ? "light_mode" : "dark_mode";
            if (label) label.textContent = dark ? "Light mode" : "Dark mode";
            button.setAttribute("aria-label", dark ? "Switch to light mode" : "Switch to dark mode");
        });
    };
    applyTheme(document.documentElement.dataset.theme || "light");
    themeButtons.forEach(button => button.addEventListener("click", () => {
        applyTheme(document.documentElement.dataset.theme === "dark" ? "light" : "dark");
    }));

    document.querySelectorAll("form[data-confirm]").forEach(form => {
        form.addEventListener("submit", event => {
            if (form.dataset.confirmed === "true") {
                delete form.dataset.confirmed;
                return;
            }
            const message = form.dataset.confirm || "Are you sure you want to continue?";
            event.preventDefault();
            const actions = document.createElement("div");
            actions.className = "toast-confirm-actions";
            const cancel = document.createElement("button");
            cancel.type = "button";
            cancel.className = "button button-secondary button-small";
            cancel.textContent = "Cancel";
            const confirm = document.createElement("button");
            confirm.type = "button";
            confirm.className = "button button-danger button-small";
            confirm.textContent = "Confirm";
            actions.append(cancel, confirm);
            const toast = showToast(message, "warning", actions);
            cancel.addEventListener("click", () => dismiss(toast));
            confirm.addEventListener("click", () => {
                dismiss(toast);
                form.dataset.confirmed = "true";
                form.requestSubmit();
            });
        });
    });

    document.querySelectorAll("[data-station-input]").forEach(input => {
        const list = document.getElementById(input.dataset.stationList);
        const wrapper = input.closest(".station-autocomplete");
        if (!list || !wrapper) return;
        const options = Array.from(list.options).map(option => option.value).filter(Boolean);
        const menu = document.createElement("div");
        menu.className = "station-suggestions";
        menu.setAttribute("role", "listbox");
        menu.hidden = true;
        wrapper.appendChild(menu);
        const render = () => {
            const query = input.value.trim().toLowerCase();
            menu.replaceChildren();
            options.filter(option => !query || option.toLowerCase().includes(query)).slice(0, 12).forEach(option => {
                const item = document.createElement("button");
                item.type = "button";
                item.className = "station-suggestion";
                item.textContent = option;
                item.setAttribute("role", "option");
                item.addEventListener("click", () => { input.value = option; menu.hidden = true; });
                menu.appendChild(item);
            });
            menu.hidden = menu.childElementCount === 0;
        };
        input.addEventListener("focus", render);
        input.addEventListener("input", render);
        document.addEventListener("click", event => { if (!wrapper.contains(event.target)) menu.hidden = true; });
    });

    const liveSearchForm = document.querySelector("form[data-live-search]");
    if (liveSearchForm) {
        let resultsRegion = document.querySelector("#search-results-content");
        let searchTimer;
        let requestSequence = 0;
        const refreshSearchResults = () => {
            const formData = new FormData(liveSearchForm);
            const passengerField = liveSearchForm.querySelector("[name='passengers']");
            const requestedPassengers = Math.max(1, Math.min(10, Number(formData.get("passengers") || 1)));
            if (passengerField) passengerField.value = String(requestedPassengers);
            formData.set("passengers", String(requestedPassengers));
            const query = new URLSearchParams(formData);
            const requestId = ++requestSequence;
            fetch(`${liveSearchForm.action}?${query.toString()}`, { headers: { "X-Requested-With": "XMLHttpRequest" } })
                .then(response => response.ok ? response.text() : Promise.reject(new Error("Search request failed")))
                .then(html => {
                    if (requestId !== requestSequence) return;
                    const nextDocument = new DOMParser().parseFromString(html, "text/html");
                    const nextRegion = nextDocument.querySelector("#search-results-content");
                    if (!nextRegion || !resultsRegion) return;
                    resultsRegion.replaceWith(nextRegion);
                    resultsRegion = nextRegion;
                    window.history.replaceState({}, "", `${liveSearchForm.action}?${query.toString()}`);
                })
                .catch(() => showToast("We could not refresh the schedule list. Try again.", "error"));
        };
        const queueSearch = () => {
            window.clearTimeout(searchTimer);
            searchTimer = window.setTimeout(refreshSearchResults, 300);
        };
        liveSearchForm.querySelectorAll("input").forEach(input => {
            input.addEventListener("input", queueSearch);
            input.addEventListener("change", queueSearch);
        });
        liveSearchForm.addEventListener("submit", event => {
            event.preventDefault();
            window.clearTimeout(searchTimer);
            refreshSearchResults();
        });
    }

    document.addEventListener("click", event => {
        document.querySelectorAll(".account-menu[open]").forEach(menu => {
            if (!menu.contains(event.target)) menu.removeAttribute("open");
        });
    });

    document.querySelectorAll("[data-table-filter]").forEach(input => {
        const table = document.getElementById(input.dataset.tableFilter);
        if (!table) return;
        const rows = Array.from(table.querySelectorAll("tbody tr[data-row]"));
        const empty = table.querySelector("tbody tr[data-filter-empty]");
        input.addEventListener("input", () => {
            const query = input.value.trim().toLowerCase();
            let visible = 0;
            rows.forEach(row => {
                const matches = !query || row.textContent.toLowerCase().includes(query);
                row.hidden = !matches;
                if (matches) visible += 1;
            });
            if (empty) empty.hidden = visible !== 0;
        });
    });

    const cardNumber = document.querySelector("[data-card-number]");
    const cardName = document.querySelector("[data-card-name]");
    const cardExpiry = document.querySelector("[data-card-expiry]");
    const previewNumber = document.querySelector("[data-card-preview-number]");
    const previewName = document.querySelector("[data-card-preview-name]");
    const previewExpiry = document.querySelector("[data-card-preview-expiry]");
    if (cardNumber) cardNumber.addEventListener("input", () => {
        cardNumber.value = cardNumber.value.replace(/\D/g, "").slice(0, 16).replace(/(.{4})/g, "$1 ").trim();
        if (previewNumber) previewNumber.textContent = cardNumber.value || "•••• •••• •••• ••••";
    });
    if (cardName) cardName.addEventListener("input", () => {
        if (previewName) previewName.textContent = cardName.value.trim().toUpperCase() || "CARDHOLDER NAME";
    });
    if (cardExpiry) cardExpiry.addEventListener("input", () => {
        const digits = cardExpiry.value.replace(/\D/g, "").slice(0, 4);
        cardExpiry.value = digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
        if (previewExpiry) previewExpiry.textContent = cardExpiry.value || "MM/YY";
    });

    const cashInput = document.querySelector("[data-cash-received]");
    const cashChange = document.querySelector("[data-cash-change]");
    const cashForm = cashInput?.closest("form[data-total]");
    if (cashInput && cashChange && cashForm) {
        const refreshChange = () => {
            const difference = Math.max(0, Number(cashInput.value || 0) - Number(cashForm.dataset.total || 0));
            cashChange.textContent = `LKR ${difference.toFixed(2)}`;
        };
        cashInput.addEventListener("input", refreshChange);
        refreshChange();
    }
});

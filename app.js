const API_ROOT = "";
const FALLBACK_IMAGE = "https://images.unsplash.com/photo-1459865264687-595d652de67e?auto=format&fit=crop&w=1200&q=80";

const qs = (selector) => document.querySelector(selector);
const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;
const params = new URLSearchParams(window.location.search);

function token() {
    return localStorage.getItem("token");
}

function requireAuth() {
    if (!token()) {
        window.location.href = "index.html";
        return false;
    }
    return true;
}

function authHeaders() {
    return {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token()}`
    };
}

async function apiGet(path, protectedRoute = false) {
    const res = await fetch(API_ROOT + path, {
        headers: protectedRoute ? {Authorization: `Bearer ${token()}`} : {}
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
        throw new Error(data.message || "Request failed");
    }
    return data;
}

function logout() {
    localStorage.clear();
    sessionStorage.clear();
    window.location.href = "index.html";
}

function greeting() {
    const hour = new Date().getHours();
    if (hour < 5) return "Good Night";
    if (hour < 12) return "Good Morning";
    if (hour < 17) return "Good Afternoon";
    if (hour < 21) return "Good Evening";
    return "Good Night";
}

function formatTime(value) {
    if (!value) return "--";
    return String(value).slice(0, 5);
}

function ratingFor(id) {
    return (4 + ((Number(id || 1) % 8) / 10)).toFixed(1);
}

function initTopbar() {
    qs("[data-logout]")?.addEventListener("click", logout);
}

function initHome() {
    if (!requireAuth()) return;

    const name = localStorage.getItem("userName") || "Player";
    qs("#greeting").textContent = `Hello, ${name}! ${greeting()}`;

    let selectedSport = "Cricket";
    document.querySelectorAll(".sport-card").forEach((card) => {
        card.addEventListener("click", () => {
            document.querySelectorAll(".sport-card").forEach((item) => item.classList.remove("selected"));
            card.classList.add("selected");
            selectedSport = card.dataset.sport;
        });
    });

    qs("#locationBtn")?.addEventListener("click", () => {
        const status = qs("#locationStatus");
        status.textContent = "Detecting location...";
        navigator.geolocation.getCurrentPosition(async (position) => {
            try {
                const {latitude, longitude} = position.coords;
                const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}`;
                const res = await fetch(url);
                const data = await res.json();
                const address = data.address || {};
                qs("#city").value = address.city || address.town || address.village || address.state || "";
                status.textContent = "Location added";
            } catch (error) {
                status.textContent = "Could not read city from location";
            }
        }, () => {
            status.textContent = "Location permission was not granted";
        });
    });

    qs("#findForm")?.addEventListener("submit", (event) => {
        event.preventDefault();
        const city = qs("#city").value.trim();
        if (!city) {
            qs("#homeError").textContent = "Enter a city or use your location";
            return;
        }
        window.location.href = `turfs.html?city=${encodeURIComponent(city)}&sport=${encodeURIComponent(selectedSport)}`;
    });
}

function renderTurfCard(turf) {
    return `
        <article class="card turf-card" data-price="${Number(turf.weekdayPricePerHour || 0)}">
            <img src="${turf.imageUrl || FALLBACK_IMAGE}" alt="${turf.name}">
            <div class="card-body">
                <div class="meta">
                    <span class="pill">${turf.sport}</span>
                    <span class="pill price">From ${money(turf.weekdayPricePerHour)}/hr</span>
                </div>
                <h3>${turf.name}</h3>
                <p class="meta">⌖ ${turf.city}</p>
                <p class="stars">★ ★ ★ ★ ☆ <strong>${ratingFor(turf.id)}</strong></p>
                <a class="btn secondary" href="turf-detail.html?turfId=${turf.id}">View Details</a>
            </div>
        </article>`;
}

function initTurfs() {
    const city = params.get("city") || "";
    const sport = params.get("sport") || "";
    const isOtherSport = sport.toLowerCase() === "other";
    const searchSport = isOtherSport ? "" : sport;
    const sportLabel = isOtherSport ? "All sports" : (sport || "Sports");

    qs("#listingTitle").textContent = `${sportLabel} turfs in ${city || "your city"}`;
    qs("#listingSub").textContent = "Compare hourly rates and choose the court that fits your match.";

    let turfs = [];
    let filtered = [];
    let showingFallback = false;

    const render = () => {
        const sort = qs("#sortPrice").value;
        const maxPrice = Number(qs("#priceRange").value);
        filtered = turfs
                .filter((turf) => Number(turf.weekdayPricePerHour || 0) <= maxPrice)
                .sort((a, b) => sort === "desc"
                        ? Number(b.weekdayPricePerHour) - Number(a.weekdayPricePerHour)
                        : Number(a.weekdayPricePerHour) - Number(b.weekdayPricePerHour));

        const grid = qs("#turfGrid");
        if (!filtered.length) {
            grid.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <div class="empty-illustration"></div>
                    <h2>No turfs found</h2>
                    <p class="meta">Try another city, sport, or price range.</p>
                </div>`;
            return;
        }
        grid.innerHTML = filtered.map(renderTurfCard).join("");
    };

    const updateRangeAndRender = () => {
        const max = Math.max(500, ...turfs.map((turf) => Number(turf.weekdayPricePerHour || 0)));
        qs("#priceRange").max = String(max);
        qs("#priceRange").value = String(max);
        qs("#priceValue").textContent = money(max);
        render();
    };

    const requestedPath = `/api/turfs?city=${encodeURIComponent(city)}&sport=${encodeURIComponent(searchSport)}`;
    apiGet(requestedPath)
        .then(async (data) => {
            turfs = data;
            if (!turfs.length && (city || sport)) {
                showingFallback = true;
                turfs = await apiGet("/api/turfs");
            }

            if (showingFallback && turfs.length) {
                qs("#listingTitle").textContent = "Available turfs";
                qs("#listingSub").textContent = `No exact matches for ${sport || "sports"} in ${city || "your city"}. Showing all available turfs.`;
            }

            updateRangeAndRender();
        })
        .catch((error) => {
            qs("#turfGrid").innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;"><p>${error.message}</p></div>`;
        });

    qs("#priceRange")?.addEventListener("input", () => {
        qs("#priceValue").textContent = money(qs("#priceRange").value);
        render();
    });
    qs("#sortPrice")?.addEventListener("change", render);
}

function initTurfDetail() {
    const turfId = params.get("turfId");
    if (!turfId) {
        qs("#detailRoot").innerHTML = `<div class="empty-state"><p>Turf not found</p></div>`;
        return;
    }
    apiGet(`/api/turfs/${turfId}`)
        .then((turf) => {
            qs("#detailImage").src = turf.imageUrl || FALLBACK_IMAGE;
            qs("#detailImage").alt = turf.name;
            qs("#detailName").textContent = turf.name;
            qs("#detailMeta").textContent = `⌖ ${turf.city} · ${turf.sport} · Capacity ${turf.capacity || "--"}`;
            qs("#detailDescription").textContent = turf.description || "";
            qs("#weekdayPrice").textContent = `${money(turf.weekdayPricePerHour)} per hour`;
            qs("#weekendPrice").textContent = `${money(turf.weekendPricePerHour)} per hour`;
            qs("#lightsPrice").textContent = `${money(turf.lightsSurcharge)} extra per hour`;
            qs("#amenities").innerHTML = (turf.amenities || []).map((item) => `<div class="amenity">✓ ${item}</div>`).join("");
            qs("#bookNow").href = `slots.html?turfId=${turf.id}`;
        })
        .catch((error) => {
            qs("#detailRoot").innerHTML = `<div class="empty-state"><p>${error.message}</p></div>`;
        });
}

function bookingSummaryRows(booking) {
    return `
        <div class="summary-line"><span>Reference</span><strong>${booking.bookingRef || "--"}</strong></div>
        <div class="summary-line"><span>Turf</span><strong>${booking.turfName || "--"}</strong></div>
        <div class="summary-line"><span>Location</span><strong>${booking.location || "--"}</strong></div>
        <div class="summary-line"><span>Sport</span><strong>${booking.sport || "--"}</strong></div>
        <div class="summary-line"><span>Date</span><strong>${booking.date || "--"}</strong></div>
        <div class="summary-line"><span>Time</span><strong>${formatTime(booking.startTime)} - ${formatTime(booking.endTime)}</strong></div>
        <div class="summary-line"><span>Amount Paid</span><strong>${money(booking.amount)}</strong></div>
        <div class="summary-line"><span>Transaction ID</span><strong>${booking.transactionId || "--"}</strong></div>`;
}

async function initConfirmation() {
    if (!requireAuth()) return;

    let booking = JSON.parse(sessionStorage.getItem("latestBooking") || "null");
    const ref = params.get("ref");
    if ((!booking || booking.bookingRef !== ref) && ref) {
        booking = await apiGet(`/api/bookings/${encodeURIComponent(ref)}`, true);
    }
    if (!booking) {
        qs("#confirmationSummary").innerHTML = `<p>No booking summary found.</p>`;
        return;
    }

    qs("#bookingRef").textContent = booking.bookingRef || ref || "--";
    qs("#confirmationSummary").innerHTML = bookingSummaryRows(booking);

    qs("#viewBookings")?.addEventListener("click", async () => {
        const list = await apiGet("/api/bookings/my", true);
        qs("#bookingHistory").classList.remove("hidden");
        qs("#bookingHistory").innerHTML = list.length
                ? list.map((item) => `
                    <div class="booking-item">
                        <strong>${item.bookingRef}</strong>
                        <div class="meta">${item.turfName} · ${item.date} · ${formatTime(item.startTime)}-${formatTime(item.endTime)} · ${money(item.amount)}</div>
                    </div>`).join("")
                : `<div class="empty-state"><div class="empty-illustration"></div><p>No bookings yet.</p></div>`;
    });
}

document.addEventListener("DOMContentLoaded", () => {
    initTopbar();
    const page = document.body.dataset.page;
    if (page === "home") initHome();
    if (page === "turfs") initTurfs();
    if (page === "detail") initTurfDetail();
    if (page === "confirmation") initConfirmation();
});

// src/main/resources/static/js/slots.js
const SLOT_API_ROOT = "";
const slotParams = new URLSearchParams(window.location.search);
const slotMoney = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;
const slotEl = (selector) => document.querySelector(selector);

let currentTurf = null;
let selectedSlot = null;
let selectedAmount = 0;

function slotToken() {
    return localStorage.getItem("token");
}

function requireSlotAuth() {
    if (!slotToken()) {
        window.location.href = "index.html";
        return false;
    }
    return true;
}

function slotLogout() {
    localStorage.clear();
    sessionStorage.clear();
    window.location.href = "index.html";
}

function slotFormatTime(value) {
    return value ? String(value).slice(0, 5) : "--";
}

function toDateInputValue(date) {
    return date.toISOString().slice(0, 10);
}

function addDays(date, days) {
    const copy = new Date(date);
    copy.setDate(copy.getDate() + days);
    return copy;
}

function isWeekend(dateValue) {
    const day = new Date(`${dateValue}T00:00:00`).getDay();
    return day === 0 || day === 6;
}

function showSlotToast(message, type = "success") {
    const toast = slotEl("#toast");
    if (!toast) return;
    toast.textContent = message;
    toast.className = `toast ${type}`;
    window.setTimeout(() => toast.classList.add("hidden"), 2600);
}

async function slotGet(path) {
    const response = await fetch(SLOT_API_ROOT + path);
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.message || "Request failed");
    }
    return data;
}

function updateSummary() {
    const dateValue = slotEl("#slotDate").value;
    const base = currentTurf
        ? Number(isWeekend(dateValue) ? currentTurf.weekendPricePerHour : currentTurf.weekdayPricePerHour)
        : 0;
    const lights = slotEl("#lightsToggle").checked ? Number(currentTurf?.lightsSurcharge || 0) : 0;
    selectedAmount = selectedSlot ? base + lights : 0;

    slotEl("#summaryDate").textContent = selectedSlot ? dateValue : "--";
    slotEl("#summaryTime").textContent = selectedSlot
        ? `${slotFormatTime(selectedSlot.startTime)} - ${slotFormatTime(selectedSlot.endTime)}`
        : "--";
    slotEl("#summaryBase").textContent = selectedSlot ? slotMoney(base) : slotMoney(0);
    slotEl("#summaryLights").textContent = selectedSlot ? slotMoney(lights) : slotMoney(0);
    slotEl("#summaryTotal").textContent = slotMoney(selectedAmount);
    slotEl("#payButton").disabled = !selectedSlot;
}

function renderSlots(slots) {
    const grid = slotEl("#slotGrid");
    selectedSlot = null;
    updateSummary();

    if (!slots.length) {
        grid.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-illustration"></div>
                <h2>No slots found</h2>
                <p class="meta">Choose another date.</p>
            </div>`;
        return;
    }

    grid.innerHTML = slots
        .sort((a, b) => String(a.startTime).localeCompare(String(b.startTime)))
        .map((slot) => {
            const status = String(slot.status || "AVAILABLE").toUpperCase();
            const statusClass = status === "BOOKED" ? "booked" : "available";
            const disabled = status === "BOOKED" ? "disabled" : "";
            return `
                <button class="slot-tile ${statusClass}" type="button" data-slot-id="${slot.id}" ${disabled}>
                    <span>${slotFormatTime(slot.startTime)}</span>
                    <small>${status}</small>
                </button>`;
        })
        .join("");

    grid.querySelectorAll(".slot-tile.available").forEach((button) => {
        button.addEventListener("click", () => {
            grid.querySelectorAll(".slot-tile").forEach((tile) => tile.classList.remove("selected"));
            button.classList.add("selected");
            selectedSlot = slots.find((slot) => String(slot.id) === button.dataset.slotId);
            updateSummary();
        });
    });
}

async function loadSlots() {
    const turfId = slotParams.get("turfId");
    const dateValue = slotEl("#slotDate").value;
    slotEl("#slotStatus").textContent = "Loading slots...";
    slotEl("#slotGrid").innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;"><div class="spinner" aria-hidden="true"></div></div>`;
    try {
        const slots = await slotGet(`/api/slots/turf/${encodeURIComponent(turfId)}?date=${encodeURIComponent(dateValue)}`);
        renderSlots(slots);
        slotEl("#slotStatus").textContent = `${slots.length} slots for ${dateValue}`;
    } catch (error) {
        slotEl("#slotStatus").textContent = error.message;
        slotEl("#slotGrid").innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;"><p>${error.message}</p></div>`;
    }
}

async function initSlotsPage() {
    if (!requireSlotAuth()) return;

    document.querySelectorAll("[data-logout]").forEach((button) => {
        button.addEventListener("click", slotLogout);
    });

    const turfId = slotParams.get("turfId");
    if (!turfId) {
        slotEl("#slotTurfName").textContent = "Turf not found";
        slotEl("#slotStatus").textContent = "Missing turf id";
        return;
    }

    const today = new Date();
    const dateInput = slotEl("#slotDate");
    dateInput.min = toDateInputValue(today);
    dateInput.max = toDateInputValue(addDays(today, 7));
    dateInput.value = toDateInputValue(today);

    try {
        currentTurf = await slotGet(`/api/turfs/${encodeURIComponent(turfId)}`);
        slotEl("#slotTurfName").textContent = currentTurf.name;
        slotEl("#slotTurfMeta").textContent = `${currentTurf.city} · ${currentTurf.sport}`;
        await loadSlots();
    } catch (error) {
        slotEl("#slotTurfName").textContent = "Could not load turf";
        slotEl("#slotStatus").textContent = error.message;
    }

    dateInput.addEventListener("change", loadSlots);
    slotEl("#lightsToggle").addEventListener("change", updateSummary);
    slotEl("#payButton").addEventListener("click", () => {
        if (!selectedSlot || !currentTurf) {
            showSlotToast("Select one available slot", "error");
            return;
        }

        sessionStorage.setItem("pendingBooking", JSON.stringify({
            turfId: Number(turfId),
            slotId: selectedSlot.id,
            amount: selectedAmount,
            turfName: currentTurf.name,
            location: currentTurf.city,
            sport: currentTurf.sport,
            date: dateInput.value,
            startTime: selectedSlot.startTime,
            endTime: selectedSlot.endTime
        }));
        window.location.href = "payment.html";
    });
}

document.addEventListener("DOMContentLoaded", initSlotsPage);

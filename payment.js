// src/main/resources/static/js/payment.js
const PAYMENT_API_ROOT = "";
const paymentEl = (selector) => document.querySelector(selector);
const paymentMoney = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;
const paymentSleep = (ms) => new Promise((resolve) => window.setTimeout(resolve, ms));

let pendingBooking = null;

function paymentToken() {
    return localStorage.getItem("token");
}

function requirePaymentAuth() {
    if (!paymentToken()) {
        window.location.href = "index.html";
        return false;
    }
    return true;
}

function paymentLogout() {
    localStorage.clear();
    sessionStorage.clear();
    window.location.href = "index.html";
}

function paymentFormatTime(value) {
    return value ? String(value).slice(0, 5) : "--";
}

function showPaymentToast(message, type = "success") {
    const toast = paymentEl("#toast");
    if (!toast) return;
    toast.textContent = message;
    toast.className = `toast ${type}`;
    window.setTimeout(() => toast.classList.add("hidden"), 2600);
}

function setPaymentLoading(isLoading) {
    const button = paymentEl("#confirmPayment");
    const label = button.querySelector("[data-button-label]");
    button.disabled = isLoading;
    label.textContent = isLoading ? "Processing..." : "Confirm Payment";
    paymentEl("#paymentSpinner").classList.toggle("hidden", !isLoading);
}

async function paymentPost(path, body) {
    const response = await fetch(PAYMENT_API_ROOT + path, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${paymentToken()}`
        },
        body: JSON.stringify(body)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.message || "Payment confirmation failed");
    }
    return data;
}

function renderPaymentSummary() {
    paymentEl("#paymentSummary").innerHTML = `
        <div class="summary-line"><span>Turf</span><strong>${pendingBooking.turfName || "--"}</strong></div>
        <div class="summary-line"><span>Date</span><strong>${pendingBooking.date || "--"}</strong></div>
        <div class="summary-line"><span>Time</span><strong>${paymentFormatTime(pendingBooking.startTime)} - ${paymentFormatTime(pendingBooking.endTime)}</strong></div>
        <div class="summary-line"><span>Total Amount</span><strong>${paymentMoney(pendingBooking.amount)}</strong></div>`;

    const draftId = `${pendingBooking.turfId || "TURF"}-${pendingBooking.slotId || Date.now()}`;
    paymentEl("#qrImage").src = `https://api.qrserver.com/v1/create-qr-code/?data=TURFPAY-${encodeURIComponent(draftId)}&size=200x200`;
    paymentEl("#amount").value = Number(pendingBooking.amount || 0);
}

async function confirmPayment(event) {
    event.preventDefault();
    paymentEl("#paymentMessage").textContent = "";

    const amount = Number(paymentEl("#amount").value);
    if (!amount || amount <= 0) {
        paymentEl("#paymentMessage").textContent = "Enter an amount greater than 0";
        return;
    }

    const transactionId = `TXN-${Date.now()}`;
    setPaymentLoading(true);
    try {
        await paymentSleep(1500);
        const booking = await paymentPost("/api/bookings", {
            slotId: pendingBooking.slotId,
            turfId: pendingBooking.turfId,
            amount,
            transactionId
        });
        sessionStorage.setItem("latestBooking", JSON.stringify(booking));
        sessionStorage.removeItem("pendingBooking");
        showPaymentToast("Payment confirmed");
        window.location.href = `confirmation.html?ref=${encodeURIComponent(booking.bookingRef)}`;
    } catch (error) {
        paymentEl("#paymentMessage").textContent = error.message;
        showPaymentToast(error.message, "error");
    } finally {
        setPaymentLoading(false);
    }
}

function initPaymentPage() {
    if (!requirePaymentAuth()) return;

    document.querySelectorAll("[data-logout]").forEach((button) => {
        button.addEventListener("click", paymentLogout);
    });

    pendingBooking = JSON.parse(sessionStorage.getItem("pendingBooking") || "null");
    if (!pendingBooking) {
        paymentEl("#paymentSummary").innerHTML = `<div class="empty-state"><p>No pending booking found.</p></div>`;
        paymentEl("#confirmPayment").disabled = true;
        return;
    }

    renderPaymentSummary();
    paymentEl("#paymentForm").addEventListener("submit", confirmPayment);
}

document.addEventListener("DOMContentLoaded", initPaymentPage);

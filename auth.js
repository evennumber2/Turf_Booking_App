const API = "";

const $ = (selector) => document.querySelector(selector);

function setButtonLabel(button, labelText) {
    if (!button) return;
    const label = button.querySelector("[data-button-label]");
    if (label) {
        label.textContent = labelText;
    } else {
        button.textContent = labelText;
    }
    button.dataset.defaultLabel = labelText;
}

function setButtonLoading(button, isLoading, loadingText) {
    if (!button) return;
    const label = button.querySelector("[data-button-label]");
    const spinner = button.querySelector(".spinner");
    if (!button.dataset.defaultLabel) {
        button.dataset.defaultLabel = label ? label.textContent : button.textContent;
    }
    button.disabled = isLoading;
    if (label) {
        label.textContent = isLoading && loadingText ? loadingText : button.dataset.defaultLabel;
    }
    spinner?.classList.toggle("hidden", !isLoading);
}

function setMessage(id, message, type = "error") {
    const el = $(id);
    if (!el) return;
    el.textContent = message || "";
    el.className = type;
}

async function request(path, options = {}) {
    const res = await fetch(API + path, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });
    const contentType = res.headers.get("content-type") || "";
    const data = contentType.includes("application/json") ? await res.json() : {};
    if (!res.ok) {
        throw new Error(data.message || "Request failed");
    }
    return data;
}

function saveAuth(data) {
    localStorage.setItem("token", data.token);
    localStorage.setItem("userName", data.name || "Player");
    window.location.href = "home.html";
}

const state = {
    registerOtpSent: false,
    otpDestination: ""
};

document.querySelectorAll("[data-tab]").forEach((button) => {
    button.addEventListener("click", () => {
        document.querySelectorAll("[data-tab]").forEach((tab) => tab.classList.remove("active"));
        button.classList.add("active");
        const tab = button.dataset.tab;
        $("#loginForm").classList.toggle("hidden", tab !== "login");
        $("#registerForm").classList.toggle("hidden", tab !== "register");
        $("#forgotForm").classList.add("hidden");
    });
});

$("#forgotLink")?.addEventListener("click", () => {
    $("#forgotForm").classList.toggle("hidden");
});

$("#loginForm")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage("#loginError", "");
    const submitButton = event.submitter || $("#loginSubmit");
    const identifier = $("#loginIdentifier").value.trim();
    const password = $("#loginPassword").value;
    if (!identifier || !password) {
        setMessage("#loginError", "Enter your email/mobile and password");
        return;
    }
    setButtonLoading(submitButton, true, "Signing in...");
    try {
        const data = await request("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({identifier, password})
        });
        saveAuth(data);
    } catch (error) {
        setMessage("#loginError", error.message);
    } finally {
        setButtonLoading(submitButton, false);
    }
});

$("#registerForm")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage("#registerError", "");
    setMessage("#registerSuccess", "", "success");
    const submitButton = event.submitter || $("#registerSubmit");

    const payload = {
        name: $("#regName").value.trim(),
        email: $("#regEmail").value.trim(),
        mobile: $("#regMobile").value.trim(),
        password: $("#regPassword").value,
        confirmPassword: $("#regConfirmPassword").value
    };

    if (!payload.name || !payload.email || !payload.mobile || !payload.password || !payload.confirmPassword) {
        setMessage("#registerError", "Fill all registration fields");
        return;
    }
    if (!/^\d{10}$/.test(payload.mobile)) {
        setMessage("#registerError", "Mobile number must be 10 digits");
        return;
    }
    if (payload.password !== payload.confirmPassword) {
        setMessage("#registerError", "Passwords do not match");
        return;
    }

    setButtonLoading(submitButton, true, state.registerOtpSent ? "Creating account..." : "Sending OTP...");
    try {
        if (!state.registerOtpSent) {
            state.otpDestination = payload.mobile || payload.email;
            await request("/api/auth/send-otp", {
                method: "POST",
                body: JSON.stringify({destination: state.otpDestination})
            });
            state.registerOtpSent = true;
            $("#otpBlock").classList.remove("hidden");
            setButtonLabel($("#registerSubmit"), "Verify & Create Account");
            setMessage("#registerSuccess", "OTP sent. Check the Spring Boot console.", "success");
            return;
        }

        const otp = $("#otp").value.trim();
        if (!/^\d{6}$/.test(otp)) {
            setMessage("#registerError", "Enter the 6-digit OTP");
            return;
        }
        const verify = await request("/api/auth/verify-otp", {
            method: "POST",
            body: JSON.stringify({destination: state.otpDestination, otp})
        });
        if (!verify.verified) {
            setMessage("#registerError", "Invalid or expired OTP");
            return;
        }
        const data = await request("/api/auth/register", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        saveAuth(data);
    } catch (error) {
        setMessage("#registerError", error.message);
    } finally {
        setButtonLoading(submitButton, false);
    }
});

$("#resendOtp")?.addEventListener("click", async () => {
    setMessage("#registerError", "");
    try {
        const mobile = $("#regMobile").value.trim();
        const email = $("#regEmail").value.trim();
        state.otpDestination = mobile || email;
        await request("/api/auth/send-otp", {
            method: "POST",
            body: JSON.stringify({destination: state.otpDestination})
        });
        setMessage("#registerSuccess", "New OTP sent. Check the Spring Boot console.", "success");
    } catch (error) {
        setMessage("#registerError", error.message);
    }
});

$("#forgotForm")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage("#forgotMessage", "", "success");
    const submitButton = event.submitter || $("#forgotSubmit");
    const destination = $("#forgotDestination").value.trim();
    if (!destination) {
        setMessage("#forgotMessage", "Enter email or mobile");
        return;
    }
    setButtonLoading(submitButton, true, "Sending...");
    try {
        await request("/api/auth/forgot-password", {
            method: "POST",
            body: JSON.stringify({destination})
        });
        setMessage("#forgotMessage", "Reset link sent. Check the Spring Boot console.", "success");
    } catch (error) {
        setMessage("#forgotMessage", error.message);
    } finally {
        setButtonLoading(submitButton, false);
    }
});

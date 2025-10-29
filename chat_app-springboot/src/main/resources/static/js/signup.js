"use strict";

var signupForm = document.querySelector("#signupForm");

async function signup(event) {
    event.preventDefault();

    var username = document.querySelector("#username").value.trim();
    var email = document.querySelector("#email").value.trim();
    var password = document.querySelector("#password").value;
    var confirmPassword = document.querySelector("#confirmPassword").value;
    var mes = document.getElementById("mes");

    // Clear previous messages
    mes.innerText = "";

    // Validation
    if (!username) {
        mes.innerText = "Please enter a username";
        return;
    }

    if (username.length < 3) {
        mes.innerText = "Username must be at least 3 characters long";
        return;
    }

    if (!email) {
        mes.innerText = "Please enter an email";
        return;
    }

    // Email validation regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        mes.innerText = "Please enter a valid email address";
        return;
    }

    if (!password) {
        mes.innerText = "Please enter a password";
        return;
    }

    if (password.length < 6) {
        mes.innerText = "Password must be at least 6 characters long";
        return;
    }

    if (password !== confirmPassword) {
        mes.innerText = "Passwords do not match";
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/api/users/signup", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: username,
                email: email,
                password: password
            })
        });

        const result = await response.text();

        if (response.ok) {
            mes.style.color = "#4ade80";
            mes.innerText = "Account created successfully! Redirecting to login...";
            setTimeout(() => {
                window.location.href = "login.html";
            }, 2000);
        } else {
            mes.style.color = "red";
            mes.innerText = result || "Signup failed. Try again.";
        }

    } catch (error) {
        mes.style.color = "red";
        mes.innerText = "Error connecting to server.";
        console.error(error);
    }
}

signupForm.addEventListener("submit", signup, true);

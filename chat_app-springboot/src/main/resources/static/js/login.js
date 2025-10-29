"use strict";

var loginForm = document.querySelector("#usernameForm");

async function login(event) {
    event.preventDefault();

    var username = document.querySelector("#name").value.trim();
    var password = document.querySelector("#password").value;
    var mes = document.getElementById("mes");

    mes.innerText = "";

    if (!username || !password) {
        mes.innerText = "Please enter both username and password.";
        return;
    }

    try {
        const requestBody = {
            username: username,
            password: password
        };
        console.log('Sending login request with:', requestBody);

        const response = await fetch("http://localhost:8080/api/users/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        });

        console.log('Response status:', response.status);

        const result = await response.text();

        if (response.ok) {
            mes.style.color = "#4ade80";
            mes.innerText = "Login successful! Redirecting...";
            setTimeout(() => {
                window.location.href = "chat.html?user=" + encodeURIComponent(username);
            }, 1000);
        } else {
            mes.style.color = "red";
            mes.innerText = result || "Login failed. Please check your credentials.";
        }
    } catch (error) {
        mes.style.color = "red";
        mes.innerText = "Error connecting to server.";
        console.error(error);
    }
}

loginForm.addEventListener("submit", login, true);


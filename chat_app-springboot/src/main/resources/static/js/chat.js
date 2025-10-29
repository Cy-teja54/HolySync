"use strict";

var messageForm = document.querySelector("#messageForm");
var messageInput = document.querySelector("#message");
var messageArea = document.querySelector("#messageArea");
var connectingElement = document.querySelector(".connecting");


var stompClient = null;
var username = null;

var colors = [
    "#2196F3", "#32c787", "#00BCD4", "#ff5652",
    "#ffc107", "#ff85af", "#FF9800", "#39bbb0",
    "#fcba03", "#fc0303", "#de5454", "#b9de54",
    "#54ded7", "#54ded7", "#1358d6", "#d611c6"
];

// Get username from URL parameter
function getUsernameFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('user');
}

// Check if user is logged in
function checkAuth() {
    username = getUsernameFromURL();
    console.log("Checking auth, username from URL:", username);
    
    if (!username) {
        console.log("No username found, redirecting to login");
        window.location.href = "login.html";
        return false;
    }
    
    console.log("User authenticated:", username);
    return true;
}

// Logout function
function logout() {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect();
    }
    window.location.href = "login.html";
}

// Connect to WebSocket
function connectToChat() {
    var socket = new SockJS("/websocket");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    console.log("WebSocket connected!");
    
    // Subscribe to the Public Topic
    stompClient.subscribe("/topic/public", onMessageReceived);

    // Tell your username to the server
    stompClient.send(
        "/app/chat.register",
        {},
        JSON.stringify({ sender: username, type: "JOIN" })
    );

    connectingElement.classList.add("hidden");
}

function onError(error) {
    console.error("WebSocket error:", error);
    connectingElement.textContent =
        "Could not connect to WebSocket! Please refresh the page and try again or contact your administrator.";
    connectingElement.style.color = "red";
}

function send(event) {
    event.preventDefault();
    
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: "CHAT"
        };

        stompClient.send("/app/chat.send", {}, JSON.stringify(chatMessage));
        messageInput.value = "";
    }
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement("li");

    if (message.type === "JOIN") {
        messageElement.classList.add("event-message");
        message.content = message.sender + " joined!";
    } else if (message.type === "LEAVE") {
        messageElement.classList.add("event-message");
        message.content = message.sender + " left!";
    } else {
        messageElement.classList.add("chat-message");

        var avatarElement = document.createElement("i");
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style["background-color"] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement("span");
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
        usernameElement.style["color"] = getAvatarColor(message.sender);
    }

    var textElement = document.createElement("p");
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);
    
    if (message.sender === username && message.type === "CHAT") {
        messageElement.classList.add("own-message");
    }
    
    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

// Initialize
console.log("chat.js loaded");
if (checkAuth()) {
    console.log("Starting WebSocket connection...");
    connectToChat();
}

messageForm.addEventListener("submit", send, true);
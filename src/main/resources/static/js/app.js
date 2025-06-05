// app.js
// Refined chat logic: ensure history renders once and avoid duplicates

document.addEventListener('DOMContentLoaded', () => {
    const username = sessionStorage.getItem('username');
    if (!username) {
        window.location.href = 'login.html';
        return;
    }

    // Display the current user
    const currentUserEl = document.getElementById('currentUser');
    if (currentUserEl) currentUserEl.textContent = username;

    // Handle logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            sessionStorage.removeItem('username');
            window.location.href = 'login.html';
        });
    }

    // DOM references
    const usersList = document.getElementById('usersList');
    const messagesContainer = document.getElementById('messages');
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('messageInput');

    // Helper to render a single message
    function renderMessage(msg) {
        const wrapper = document.createElement('div');
        wrapper.classList.add('message');
        if (msg.sender === 'System') wrapper.classList.add('system');

        const userSpan = document.createElement('span');
        userSpan.classList.add('username');
        userSpan.textContent = msg.sender;

        const contentSpan = document.createElement('span');
        contentSpan.classList.add('message-content');
        contentSpan.textContent = ` [${new Date(msg.timestamp).toLocaleTimeString()}] ${msg.content}`;

        wrapper.append(userSpan, contentSpan);
        messagesContainer.appendChild(wrapper);
    }

    // Establish the WebSocket connection
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({ username }, () => {
        // 1. Subscribe to personal history queue
        stompClient.subscribe('/user/queue/history', frame => {
            const history = JSON.parse(frame.body);
            // Clear existing messages once
            messagesContainer.innerHTML = '';
            history.forEach(msg => renderMessage(msg));
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        });
        // Request history after subscription is active
        stompClient.send('/app/chat.history', {}, {});

        // 2. Subscribe to broadcasted messages
        stompClient.subscribe('/topic/messages', frame => {
            const payload = JSON.parse(frame.body);
            // Only new messages should be processed here
            const messages = Array.isArray(payload) ? payload : [payload];
            messages.forEach(msg => {
                renderMessage(msg);
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            });
        });

        // 3. Subscribe to presence updates
        stompClient.subscribe('/topic/presence', frame => {
            const users = JSON.parse(frame.body);
            usersList.innerHTML = users
                .map(u => `<li class="${u === username ? 'self' : ''}">${u}${u === username ? ' (You)' : ''}</li>`)
                .join('');
        });
        // Request current presence list
        stompClient.send('/app/presence', {}, {});
    });

    // Handle sending a new message
    messageForm.addEventListener('submit', e => {
        e.preventDefault();
        const content = messageInput.value.trim();
        if (!content) return;
        stompClient.send('/app/chat.send', {}, JSON.stringify({ sender: username, content }));
        messageInput.value = '';
    });
});



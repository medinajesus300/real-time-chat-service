

document.addEventListener('DOMContentLoaded', () => {
    const username = sessionStorage.getItem('username');
    if (!username) {
        window.location.href = 'login.html';
        return;
    }

    // Display current user
    const currentUserEl = document.getElementById('currentUser');
    if (currentUserEl) currentUserEl.textContent = username;

    // Logout handler
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

    // Establish WebSocket connection
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({ username }, () => {
        // Subscribe to personal history queue
        stompClient.subscribe('/user/queue/history', frame => {
            const history = JSON.parse(frame.body);
            // Clear existing messages
            messagesContainer.innerHTML = '';
            history.forEach(msg => renderMessage(msg));
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        });
        // Request history
        stompClient.send('/app/chat.history', {}, {});

        // Subscribe to broadcasted messages
        stompClient.subscribe('/topic/messages', frame => {
            const payload = JSON.parse(frame.body);
            const messages = Array.isArray(payload) ? payload : [payload];
            messages.forEach(msg => {
                renderMessage(msg);
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            });
        });

        // Subscribe to presence updates
        stompClient.subscribe('/topic/presence', frame => {
            const users = JSON.parse(frame.body);
            usersList.innerHTML = users
                .map(u => `<li class="${u === username ? 'self' : ''}">${u}${u === username ? ' (You)' : ''}</li>`)
                .join('');
        });
        // Request presence list
        stompClient.send('/app/presence', {}, {});
    });

    // Send new messages
    messageForm.addEventListener('submit', e => {
        e.preventDefault();
        const content = messageInput.value.trim();
        if (!content) return;
        stompClient.send('/app/chat.send', {}, JSON.stringify({ sender: username, content }));
        messageInput.value = '';
    });
});


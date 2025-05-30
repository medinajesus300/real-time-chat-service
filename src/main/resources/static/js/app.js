

document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    const username = sessionStorage.getItem('username');
    if (!username) {
        window.location.href = 'login.html';
        return;
    }

    // Populate current user (static in HTML)
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

    // Establish WebSocket connection
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    // Pass username as a CONNECT header for server to assign Principal
    stompClient.connect({ username }, () => {
        // Presence subscriptions
        stompClient.subscribe('/topic/presence', frame => {
            const users = JSON.parse(frame.body);
            usersList.innerHTML = users.map(u => {
                const me = u === username ? ' (You)' : '';
                const cls = u === username ? 'self' : '';
                return `<li class="${cls}">${u}${me}</li>`;
            }).join('');
        });

        // Message subscriptions
        stompClient.subscribe('/topic/messages', frame => {
            const payload = JSON.parse(frame.body);
            (Array.isArray(payload) ? payload : [payload]).forEach(msg => {
                const msgDiv = document.createElement('div');
                msgDiv.classList.add('message');

                const userEl = document.createElement('span');
                userEl.classList.add('username');
                userEl.textContent = msg.sender;

                const contentEl = document.createElement('span');
                contentEl.classList.add('message-content');
                const time = new Date(msg.timestamp).toLocaleTimeString();
                contentEl.textContent = ` [${time}] ${msg.content}`;

                msgDiv.append(userEl, contentEl);
                messagesContainer.appendChild(msgDiv);
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            });
        });

        // Request history
        stompClient.send('/app/chat.history', {}, {});
    }, error => {
        console.error('WebSocket connection error', error);
    });

    // Send new message
    messageForm.addEventListener('submit', e => {
        e.preventDefault();
        const content = messageInput.value.trim();
        if (!content) return;
        stompClient.send('/app/chat.send', {}, JSON.stringify({ sender: username, content }));
        messageInput.value = '';
    });
});



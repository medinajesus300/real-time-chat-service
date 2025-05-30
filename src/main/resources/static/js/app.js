

document.addEventListener('DOMContentLoaded', () => {
    // Authenticate
    const username = sessionStorage.getItem('username');
    if (!username) {
        window.location.href = 'login.html';
        return;
    }

    // Header: current user and logout
    const header = document.createElement('header');
    header.classList.add('chat-header');
    header.innerHTML = `
    Logged in as: <span id="currentUser">${username}</span>
    <button id="logoutBtn">Logout</button>
  `;
    document.body.prepend(header);

    document.getElementById('logoutBtn').addEventListener('click', () => {
        sessionStorage.removeItem('username');
        fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin' })
            .finally(() => window.location.href = 'login.html');
    });

    // DOM elements
    const usersList = document.getElementById('usersList');
    const messagesContainer = document.getElementById('messages');
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('messageInput');

    // WebSocket connection
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        // Presence updates
        stompClient.subscribe('/topic/presence', frame => {
            const users = JSON.parse(frame.body);
            usersList.innerHTML = users.map(u => {
                const me = u === username ? ' (You)' : '';
                const cls = u === username ? 'self' : '';
                return `<li class="${cls}">${u}${me}</li>`;
            }).join('');
        });

        // Incoming messages
        stompClient.subscribe('/topic/messages', frame => {
            const payload = JSON.parse(frame.body);
            (Array.isArray(payload) ? payload : [payload]).forEach(msg => {
                const msgDiv = document.createElement('div');
                msgDiv.classList.add('message');

                // Username span
                const userEl = document.createElement('span');
                userEl.classList.add('username');
                userEl.textContent = msg.sender;

                // Message content
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
    });

    // Send messages
    messageForm.addEventListener('submit', e => {
        e.preventDefault();
        const content = messageInput.value.trim();
        if (!content) return;
        stompClient.send(
            '/app/chat.send',
            {},
            JSON.stringify({ sender: username, content })
        );
        messageInput.value = '';
    });
});



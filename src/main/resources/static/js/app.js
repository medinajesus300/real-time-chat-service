// Establish SockJS/STOMP connection and handle chat logic

document.addEventListener('DOMContentLoaded', () => {
    // Ensure user is authenticated
    const username = sessionStorage.getItem('username');
    if (!username) {
        window.location.href = 'login.html';
        return;
    }

    // DOM elements
    const usersList = document.getElementById('usersList');
    const messagesContainer = document.getElementById('messages');
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('messageInput');

    // Connect to WebSocket endpoint
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        // Subscribe to presence updates
        stompClient.subscribe('/topic/presence', frame => {
            const users = JSON.parse(frame.body);
            usersList.innerHTML = users.map(u => `<li>${u}</li>`).join('');
        });

        // Subscribe to incoming messages
        stompClient.subscribe('/topic/messages', frame => {
            const payload = JSON.parse(frame.body);
            (Array.isArray(payload) ? payload : [payload]).forEach(msg => {
                const el = document.createElement('div');
                const time = new Date(msg.timestamp).toLocaleTimeString();
                el.textContent = `[${time}] ${msg.sender}: ${msg.content}`;
                messagesContainer.appendChild(el);
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            });
        });

        // Request chat history
        stompClient.send('/app/chat.history', {}, {});
    });

    // Handle sending new messages
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



document.addEventListener('DOMContentLoaded', () => {
    const username = sessionStorage.getItem('username');
    if (!username) {
        window.location.href = 'login.html';
        return;
    }

    /* --------------------------------------------------
     * Cached DOM refs
     * -------------------------------------------------- */
    const currentUserEl = document.getElementById('currentUser');
    const logoutBtn     = document.getElementById('logoutBtn');
    const usersList     = document.getElementById('usersList');
    const messagesWrap  = document.getElementById('messages');
    const messageForm   = document.getElementById('messageForm');
    const messageInput  = document.getElementById('messageInput');

    if (currentUserEl) currentUserEl.textContent = username;
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            sessionStorage.removeItem('username');
            window.location.href = 'login.html';
        });
    }

    /* --------------------------------------------------
     * Deduplication helpers (just in case)
     * -------------------------------------------------- */
    const seen = new Set();
    const msgKey = m => `${m.sender}-${m.timestamp}-${m.content}`;

    function resetSeen() {
        seen.clear();
    }

    /* --------------------------------------------------
     * Render helpers
     * -------------------------------------------------- */
    function renderMessage(m) {
        // Avoid duplicates that could still slip in
        const key = msgKey(m);
        if (seen.has(key)) return;
        seen.add(key);

        const wrapper = document.createElement('div');
        wrapper.classList.add('message');
        if (m.sender === 'System') wrapper.classList.add('system');

        const userSpan = document.createElement('span');
        userSpan.classList.add('username');
        userSpan.textContent = m.sender;

        const contentSpan = document.createElement('span');
        contentSpan.classList.add('message-content');
        contentSpan.textContent = ` [${new Date(m.timestamp).toLocaleTimeString()}] ${m.content}`;

        wrapper.append(userSpan, contentSpan);
        messagesWrap.appendChild(wrapper);
    }

    function scrollToBottom() {
        messagesWrap.scrollTop = messagesWrap.scrollHeight;
    }

    /* --------------------------------------------------
     * WebSocket / STOMP setup
     * -------------------------------------------------- */
    const socket       = new SockJS('/ws');
    const stompClient  = Stomp.over(socket);

    stompClient.connect({ username }, () => {
        /* ------------- History (private queue) ------------- */
        stompClient.subscribe('/user/queue/history', frame => {
            const history = JSON.parse(frame.body);
            messagesWrap.innerHTML = '';
            resetSeen();
            history.forEach(renderMessage);
            scrollToBottom();
        });

        stompClient.send('/app/chat.history', {}, {}); // request history

        /* ------------- Live broadcast ------------- */
        stompClient.subscribe('/topic/messages', frame => {
            const payload  = JSON.parse(frame.body);
            const messages = Array.isArray(payload) ? payload : [payload];
            messages.forEach(m => {
                renderMessage(m);
                scrollToBottom();
            });
        });

        /* ------------- Presence ------------- */
        stompClient.subscribe('/topic/presence', frame => {
            const users = JSON.parse(frame.body);
            usersList.innerHTML = users
                .map(u => `<li class="${u === username ? 'self' : ''}">${u}${u === username ? ' (You)' : ''}</li>`)
                .join('');
        });
        stompClient.send('/app/presence', {}, {}); // request list
    });

    /* --------------------------------------------------
     * Send a new chat message
     * -------------------------------------------------- */
    messageForm.addEventListener('submit', e => {
        e.preventDefault();
        const content = messageInput.value.trim();
        if (!content) return;
        stompClient.send('/app/chat.send', {}, JSON.stringify({ sender: username, content }));
        messageInput.value = '';
    });
});

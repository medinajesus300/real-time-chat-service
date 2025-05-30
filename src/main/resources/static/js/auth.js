
// Handles login and registration, including session cookie and username storage

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            try {
                const res = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'same-origin',          // include session cookie
                    body: JSON.stringify({ username, password })
                });
                if (res.ok) {
                    sessionStorage.setItem('username', username);  // store for app.js
                    window.location.href = 'index.html';
                } else {
                    const msg = await res.text();
                    alert(msg || 'Login failed');
                }
            } catch (err) {
                console.error('Login error:', err);
                alert('An error occurred. Please try again.');
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            try {
                const res = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'same-origin',          // include cookie for consistency
                    body: JSON.stringify({ username, password })
                });
                if (res.ok) {
                    alert('Registration successful! You can now log in.');
                    window.location.href = 'login.html';
                } else {
                    const msg = await res.text();
                    alert(msg || 'Registration failed');
                }
            } catch (err) {
                console.error('Registration error:', err);
                alert('An error occurred. Please try again.');
            }
        });
    }
});



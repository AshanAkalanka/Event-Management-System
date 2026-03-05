// Event Management Login System
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('login-form');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.getElementById('login-btn');
    const infoDiv = document.getElementById('info');
    const failBox = document.getElementById('fail-box');
    const failText = document.getElementById('fail-text');
    const tryAgainBtn = document.getElementById('try-again');

    // Show loading state
    function showLoading() {
        loginBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Logging in...';
        loginBtn.disabled = true;
        infoDiv.classList.add('hidden');
        failBox.classList.add('hidden');
    }

    // Hide loading state
    function hideLoading() {
        loginBtn.innerHTML = 'Login';
        loginBtn.disabled = false;
    }

    // Show success message
    function showSuccess() {
        infoDiv.innerHTML = '<i class="fas fa-check-circle text-green-600"></i> Login successful...! ';
        infoDiv.classList.remove('hidden');
        infoDiv.classList.add('text-green-600');
    }

    // Show error message
    function showError(message) {
        failText.textContent = message;
        failBox.classList.remove('hidden');
        hideLoading();
    }

    // Validate credentials
    function validateCredentials(email, password) {
        return validCredentials[email] === password;
    }

    // Handle form submission
    function handleLogin(event) {
        event.preventDefault();
        
        const email = emailInput.value.trim();
        const password = passwordInput.value.trim();

        // Basic validation
        if (!email || !password) {
            showError('Please fill in all fields.');
            return;
        }

        if (!email.includes('@')) {
            showError('Please enter a valid email address.');
            return;
        }

        showLoading();

        // Send login request to server
        const formData = new FormData();
        formData.append('email', email);
        formData.append('password', password);

        fetch('/login', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showSuccess();
                
                // Store login state in sessionStorage for client-side checks
                sessionStorage.setItem('isLoggedIn', 'true');
                sessionStorage.setItem('userEmail', email);
                sessionStorage.setItem('loginTime', new Date().toISOString());
                if (data.userRole) {
                    sessionStorage.setItem('userRole', data.userRole);
                }
                
                // Redirect to the main application after short delay
                setTimeout(() => {
                    window.location.href = data.redirectUrl || '/customer/home';
                }, 1500);
            } else {
                showError(data.message || 'Invalid email or password. Please try again.');
            }
        })
        .catch(error => {
            console.error('Login error:', error);
            showError('Login failed. Please try again.');
        });
    }

    // Handle try again button
    function handleTryAgain() {
        failBox.classList.add('hidden');
        emailInput.focus();
    }

    // Check if user is already logged in
    function checkExistingLogin() {
        // Check if this is a logout redirect (URL contains logout parameter)
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('logout') === 'true') {
            // Clear all session data on logout
            sessionStorage.clear();
            // Remove logout parameter from URL
            window.history.replaceState({}, document.title, window.location.pathname);
            return;
        }
        
        // Don't auto-redirect on login page - let user login manually
        // Only check sessionStorage validity, don't redirect
        const isLoggedIn = sessionStorage.getItem('isLoggedIn');
        const loginTime = sessionStorage.getItem('loginTime');
        
        if (isLoggedIn === 'true' && loginTime) {
            // Check if login is still valid (24 hours)
            const loginDate = new Date(loginTime);
            const now = new Date();
            const hoursDiff = (now - loginDate) / (1000 * 60 * 60);
            
            if (hoursDiff >= 24) {
                // Session expired, clear storage
                sessionStorage.clear();
            }
        } else {
            // No valid session, clear storage
            sessionStorage.clear();
        }
    }

    // Event listeners
    loginForm.addEventListener('submit', handleLogin);
    loginBtn.addEventListener('click', handleLogin);
    tryAgainBtn.addEventListener('click', handleTryAgain);

    // Handle Enter key in password field
    passwordInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            handleLogin(event);
        }
    });

    // Focus on email field when page loads
    emailInput.focus();

    // Check for existing login
    checkExistingLogin();

    // Add some visual feedback
    emailInput.addEventListener('input', function() {
        if (this.value.includes('@')) {
            this.style.borderColor = '#10b981';
        } else {
            this.style.borderColor = '#d1d5db';
        }
    });

    passwordInput.addEventListener('input', function() {
        if (this.value.length >= 6) {
            this.style.borderColor = '#10b981';
        } else {
            this.style.borderColor = '#d1d5db';
        }
    });
});

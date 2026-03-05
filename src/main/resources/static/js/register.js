// Event Management Registration
document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('register-form');
    const firstNameInput = document.getElementById('firstName');
    const lastNameInput = document.getElementById('lastName');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const registerBtn = document.getElementById('register-btn');
    const infoDiv = document.getElementById('info');
    const failBox = document.getElementById('fail-box');
    const failText = document.getElementById('fail-text');
    const tryAgainBtn = document.getElementById('try-again');

    // Show loading state
    function showLoading() {
        registerBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating Account...';
        registerBtn.disabled = true;
        infoDiv.classList.add('hidden');
        failBox.classList.add('hidden');
    }

    // Hide loading state
    function hideLoading() {
        registerBtn.innerHTML = '<i class="fas fa-user-plus"></i> Create Account';
        registerBtn.disabled = false;
    }

    // Show success message
    function showSuccess() {
        infoDiv.innerHTML = '<i class="fas fa-check-circle text-green-600"></i> Account created successfully!';
        infoDiv.classList.remove('hidden');
        infoDiv.classList.add('text-green-600');
    }

    // Show error message
    function showError(message) {
        failText.textContent = message;
        failBox.classList.remove('hidden');
        hideLoading();
    }

    // Validate form data
    function validateForm() {
        const firstName = firstNameInput.value.trim();
        const lastName = lastNameInput.value.trim();
        const email = emailInput.value.trim();
        const password = passwordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();

        // Check if all fields are filled
        if (!firstName || !lastName || !email || !password || !confirmPassword) {
            showError('Please fill in all fields.');
            return false;
        }

        // Validate email format
        if (!email.includes('@') || !email.includes('.')) {
            showError('Please enter a valid email address.');
            return false;
        }

        // Validate password length
        if (password.length < 6) {
            showError('Password must be at least 6 characters long.');
            return false;
        }

        // Check if passwords match
        if (password !== confirmPassword) {
            showError('Passwords do not match.');
            return false;
        }

        return true;
    }

    // Handle form submission
    function handleRegister(event) {
        event.preventDefault();
        
        if (!validateForm()) {
            return;
        }

        showLoading();

        // Prepare form data
        const formData = new FormData();
        formData.append('firstName', firstNameInput.value.trim());
        formData.append('lastName', lastNameInput.value.trim());
        formData.append('email', emailInput.value.trim());
        formData.append('password', passwordInput.value.trim());

        // Send registration request to server
        fetch('/register', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showSuccess();
                
                // Redirect to login page after short delay
                setTimeout(() => {
                    window.location.href = '/login?registered=true';
                }, 2000);
            } else {
                showError(data.message || 'Registration failed. Please try again.');
            }
        })
        .catch(error => {
            console.error('Registration error:', error);
            showError('Registration failed. Please try again.');
        });
    }

    // Handle try again button
    function handleTryAgain() {
        failBox.classList.add('hidden');
        firstNameInput.focus();
    }

    // Event listeners
    registerForm.addEventListener('submit', handleRegister);
    registerBtn.addEventListener('click', handleRegister);
    tryAgainBtn.addEventListener('click', handleTryAgain);

    // Handle Enter key in confirm password field
    confirmPasswordInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            handleRegister(event);
        }
    });

    // Focus on first name field when page loads
    firstNameInput.focus();

    // Add visual feedback for form validation
    emailInput.addEventListener('input', function() {
        if (this.value.includes('@') && this.value.includes('.')) {
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

    confirmPasswordInput.addEventListener('input', function() {
        const password = passwordInput.value;
        if (this.value === password && password.length >= 6) {
            this.style.borderColor = '#10b981';
        } else {
            this.style.borderColor = '#d1d5db';
        }
    });
});

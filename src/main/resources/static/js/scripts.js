// JavaScript file
document.addEventListener('DOMContentLoaded', () => {
    initializeAnimations();
    initializeFormValidation();
    initializeDeleteConfirmations();
    initializeTooltips();
    initializeLoadingStates();
    initializeSmoothScrolling();
});

// Initialize animations
function initializeAnimations() {
    // card animations
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
        card.classList.add('fade-in');
    });

    // Slide up animations for sections
    const sections = document.querySelectorAll('.section, .detail-container, .form-container');
    sections.forEach((section, index) => {
        section.style.animationDelay = `${index * 0.2}s`;
        section.classList.add('slide-up');
    });
}

// Initialize form validation
function initializeFormValidation() {
    const forms = document.querySelectorAll('.needs-validation');
    
    forms.forEach(form => {
        form.addEventListener('submit', (e) => {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
                showValidationErrors(form);
            } else {
                showLoadingState(form);
            }
            form.classList.add('was-validated');
        });

        // Real-time validation
        const inputs = form.querySelectorAll('.form-control');
        inputs.forEach(input => {
            input.addEventListener('blur', () => validateField(input));
            input.addEventListener('input', () => clearFieldError(input));
        });
    });
}

// Show validation errors
function showValidationErrors(form) {
    const invalidFields = form.querySelectorAll('.form-control:invalid');
    invalidFields.forEach(field => {
        field.classList.add('is-invalid');
        const feedback = field.nextElementSibling;
        if (feedback && feedback.classList.contains('invalid-feedback')) {
            feedback.style.display = 'block';
        }
    });
}

// Validate individual field
function validateField(field) {
    const isValid = field.checkValidity();
    if (isValid) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
    } else {
        field.classList.remove('is-valid');
        field.classList.add('is-invalid');
    }
}

// Clear field error
function clearFieldError(field) {
    field.classList.remove('is-invalid');
    const feedback = field.nextElementSibling;
    if (feedback && feedback.classList.contains('invalid-feedback')) {
        feedback.style.display = 'none';
    }
}

// Initialize delete confirmations
function initializeDeleteConfirmations() {
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            showDeleteConfirmation(btn);
        });
    });
}

// Show delete confirmation modal
function showDeleteConfirmation(btn) {
    const eventName = btn.closest('.card')?.querySelector('.card-title')?.textContent || 'this item';
    
    if (confirm(`⚠️ Warning: Deleting this event will also remove all linked staff, equipment, and schedules. Do you want to continue?\n\nEvent: "${eventName}"\n\nThis action cannot be undone.`)) {
        showLoadingState(btn);
        // Use setTimeout to ensure the loading state is visible
        setTimeout(() => {
            window.location.href = btn.href;
        }, 500);
    }
}

// Initialize tooltips (small pop-up box)
function initializeTooltips() {
    // Add tooltips to buttons
    const tooltipElements = document.querySelectorAll('[data-tooltip]');
    tooltipElements.forEach(element => {
        element.addEventListener('mouseenter', showTooltip);
        element.addEventListener('mouseleave', hideTooltip);
    });
}

// Show tooltip
function showTooltip(e) {
    const tooltip = document.createElement('div');
    tooltip.className = 'custom-tooltip';
    tooltip.textContent = e.target.dataset.tooltip;
    document.body.appendChild(tooltip);
    
    const rect = e.target.getBoundingClientRect();
    tooltip.style.left = rect.left + rect.width / 2 - tooltip.offsetWidth / 2 + 'px';
    tooltip.style.top = rect.top - tooltip.offsetHeight - 8 + 'px';
}

// Hide tooltip
function hideTooltip() {
    const tooltip = document.querySelector('.custom-tooltip');
    if (tooltip) {
        tooltip.remove();
    }
}

// Initialize loading states
function initializeLoadingStates() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', () => {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                showLoadingState(submitBtn);
            }
        });
    });
}

// Show loading state
function showLoadingState(element) {
    element.classList.add('loading');
    element.disabled = true;
    
    const originalText = element.textContent;
    element.textContent = 'Loading...';
    
    // Reset after 3 seconds if still loading
    setTimeout(() => {
        element.classList.remove('loading');
        element.disabled = false;
        element.textContent = originalText;
    }, 3000);
}

// Initialize smooth scrolling
function initializeSmoothScrolling() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', (e) => {
            e.preventDefault();
            const target = document.querySelector(anchor.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// Utility function for showing notifications
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => notification.classList.add('show'), 100);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Utility function for debouncing
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Search functionality
function initializeSearch() {
    const searchInput = document.querySelector('#search-input');
    if (searchInput) {
        const searchCards = debounce((query) => {
            const cards = document.querySelectorAll('.card');
            cards.forEach(card => {
                const text = card.textContent.toLowerCase();
                const matches = text.includes(query.toLowerCase());
                card.style.display = matches ? 'block' : 'none';
            });
        }, 300);
        
        searchInput.addEventListener('input', (e) => {
            searchCards(e.target.value);
        });
    }
}

// Initialize search if the search input exists
document.addEventListener('DOMContentLoaded', initializeSearch);

// Add CSS for notifications and tooltips
const style = document.createElement('style');
style.textContent = `
    .custom-tooltip {
        position: absolute;
        background: var(--dark-color);
        color: white;
        padding: 0.5rem 1rem;
        border-radius: var(--border-radius);
        font-size: 0.875rem;
        z-index: 1000;
        pointer-events: none;
        box-shadow: var(--shadow-lg);
    }
    
    .notification {
        position: fixed;
        top: 2rem;
        right: 2rem;
        padding: 1rem 1.5rem;
        border-radius: var(--border-radius);
        color: white;
        font-weight: 500;
        z-index: 1000;
        transform: translateX(100%);
        transition: transform 0.3s ease;
    }
    
    .notification.show {
        transform: translateX(0);
    }
    
    .notification-info {
        background: var(--primary-color);
    }
    
    .notification-success {
        background: var(--success-color);
    }
    
    .notification-warning {
        background: var(--warning-color);
    }
    
    .notification-error {
        background: var(--danger-color);
    }
`;
document.head.appendChild(style);
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('booking-form');

    if (!form) {
        console.error('Booking form not found');
        return;
    }

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const eventName = document.getElementById('eventName').value;
        const venueType = document.getElementById('venueType') ? document.getElementById('venueType').value : '';
        const bookingType = document.getElementById('bookingType') ? document.getElementById('bookingType').value : '';
        const guestCount = parseInt(document.getElementById('guestCount').value);
        const eventDate = document.getElementById('eventDate').value;
        const venue = document.getElementById('venue').value;
        const specialRequests = document.getElementById('specialRequests').value;

        const menuInputs = document.querySelectorAll('input[data-menu-id]');
        const menuIds = [];
        const menuQuantities = [];
        menuInputs.forEach(input => {
            const qty = parseInt(input.value);
            if (qty > 0) {
                menuIds.push(parseInt(input.dataset.menuId));
                menuQuantities.push(qty);
            }
        });

        const serviceInputs = document.querySelectorAll('input[data-service-id]');
        const serviceIds = [];
        const serviceQuantities = [];
        serviceInputs.forEach(input => {
            const qty = parseInt(input.value);
            if (qty > 0) {
                serviceIds.push(parseInt(input.dataset.serviceId));
                serviceQuantities.push(qty);
            }
        });

        const formData = new FormData();
        formData.append('eventName', eventName);
        formData.append('venueType', venueType);
        formData.append('bookingType', bookingType);
        formData.append('guestCount', guestCount);
        formData.append('eventDate', eventDate);
        formData.append('venue', venue);
        formData.append('specialRequests', specialRequests);
        menuIds.forEach(id => formData.append('menuIds', id));
        menuQuantities.forEach(qty => formData.append('menuQuantities', qty));
        serviceIds.forEach(id => formData.append('serviceIds', id));
        serviceQuantities.forEach(qty => formData.append('serviceQuantities', qty));

        // Validate required fields
        if (!eventName || !venueType || !bookingType || !guestCount || !eventDate || !venue) {
            alert('Please fill in all required fields.');
            return;
        }

        try {
            const response = await fetch('/customer/book-event', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            if (data.success) {
                alert('Booking created successfully!');
                window.location.href = data.redirectUrl || '/customer/bookings';
            } else {
                alert('Error: ' + (data.message || 'Failed to create booking'));
            }
        } catch (error) {
            console.error('Booking error:', error);
            alert('Failed to create booking: ' + (error.message || 'Please try again later'));
        }
    });
});

function filterMenu(category) {
    if (typeof event !== 'undefined' && event.target) {
        document.querySelectorAll('.category-tab').forEach(tab => {
            tab.classList.remove('active');
        });
        event.target.classList.add('active');
    } else {
        // If called programmatically, find the tab with matching category
        document.querySelectorAll('.category-tab').forEach(tab => {
            tab.classList.remove('active');
            if (tab.getAttribute('data-category') === category ||
                (category === 'ALL' && tab.textContent.trim() === 'All')) {
                tab.classList.add('active');
            }
        });
    }

    const menuItems = document.querySelectorAll('.menu-item-card');
    menuItems.forEach(item => {
        if (category === 'ALL') {
            item.style.display = 'block';
        } else {
            const itemCategory = item.getAttribute('data-category');
            item.style.display = (itemCategory === category) ? 'block' : 'none';
        }
    });
}

function calculateTotal() {
    let mealTotal = 0;
    let serviceTotal = 0;

    document.querySelectorAll('input[data-menu-id]').forEach(input => {
        const qty = parseInt(input.value) || 0;
        const price = parseFloat(input.dataset.menuPrice) || 0;
        mealTotal += qty * price;
    });

    document.querySelectorAll('input[data-service-id]').forEach(input => {
        const qty = parseInt(input.value) || 0;
        const price = parseFloat(input.dataset.servicePrice) || 0;
        serviceTotal += qty * price;
    });

    document.getElementById('meal-total').textContent = 'Rs. ' + mealTotal.toFixed(2);
    document.getElementById('service-total').textContent = 'Rs. ' + serviceTotal.toFixed(2);
    document.getElementById('total-price').textContent = 'Rs. ' + (mealTotal + serviceTotal).toFixed(2);
}

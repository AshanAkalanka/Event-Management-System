async function approveBooking(button) {
    const id = button.getAttribute('data-approve-id');
    const noteEl = document.querySelector('textarea[data-admin-note-id="' + id + '"]');
    const adminNote = noteEl ? noteEl.value : '';

    if (!confirm('Approve this booking?')) return;

    const formData = new FormData();
    formData.append('adminNote', adminNote);

    const response = await fetch('/admin/bookings/' + id + '/approve', {
        method: 'POST',
        body: formData
    });

    const data = await response.json();
    alert(data.message || 'Done');
    if (data.success) location.reload();
}

async function declineBooking(button) {
    const id = button.getAttribute('data-decline-id');
    const noteEl = document.querySelector('textarea[data-admin-note-id="' + id + '"]');
    const adminNote = noteEl ? noteEl.value : '';

    if (!confirm('Decline this booking?')) return;

    const formData = new FormData();
    formData.append('adminNote', adminNote);

    const response = await fetch('/admin/bookings/' + id + '/decline', {
        method: 'POST',
        body: formData
    });

    const data = await response.json();
    alert(data.message || 'Done');
    if (data.success) location.reload();
}

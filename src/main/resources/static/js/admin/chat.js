const chatBox = document.getElementById('chatBox');
const chatForm = document.getElementById('chatForm');
const chatMessage = document.getElementById('chatMessage');
const customerSelect = document.getElementById('customerSelect');

const chatTargetEmail = document.getElementById('chatTargetEmail');
const sendBtn = document.getElementById('sendBtn');

function escapeHtml(str) {
    return (str || '').replace(/[&<>"']/g, function (m) {
        return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]);
    });
}

let currentMessageCount = 0;

function renderMessages(messages) {
    if (!chatBox) return;

    // If the number of messages hasn't changed, don't re-render to avoid flashing
    if (messages.length === currentMessageCount && currentMessageCount > 0) {
        return;
    }

    chatBox.innerHTML = '';

    messages.forEach(m => {
        const div = document.createElement('div');
        // Admin sends -> 'sent', Customer sends -> 'received'
        const isSelf = m.senderRole === 'ADMIN';
        div.className = 'msg ' + (isSelf ? 'sent admin-sent' : 'received');
        div.innerHTML = escapeHtml(m.message) + '<span class="msg-time">' + (m.createdAt || '') + '</span>';
        chatBox.appendChild(div);
    });

    chatBox.scrollTop = chatBox.scrollHeight;
    currentMessageCount = messages.length;
}

async function loadMessages() {
    const customerId = customerSelect ? customerSelect.value : '';
    if (!customerId) {
        if (chatBox) chatBox.innerHTML = '<div class="text-center text-muted mt-4"><i class="fas fa-inbox fa-3x mb-3" style="opacity:0.2;"></i><p>Select a customer to view messages.</p></div>';
        if (chatTargetEmail) chatTargetEmail.innerText = 'Select a customer';
        if (chatMessage) chatMessage.disabled = true;
        if (sendBtn) sendBtn.disabled = true;
        return;
    } else {
        if (chatMessage) chatMessage.disabled = false;
        if (sendBtn) sendBtn.disabled = false;
    }

    const res = await fetch('/api/admin/chat/messages?customerId=' + encodeURIComponent(customerId));
    const data = await res.json();
    if (data.success) {
        renderMessages(data.messages || []);
    }
}

async function sendMessage(text) {
    const customerId = customerSelect ? customerSelect.value : '';
    if (!customerId) {
        alert('Select a customer');
        return;
    }

    const formData = new FormData();
    formData.append('customerId', customerId);
    formData.append('message', text);

    const res = await fetch('/api/admin/chat/messages', {
        method: 'POST',
        body: formData
    });

    const data = await res.json();
    if (!data.success) {
        alert(data.message || 'Failed');
    }
}

if (customerSelect) {
    customerSelect.addEventListener('change', function () {
        if (this.options[this.selectedIndex] && chatTargetEmail) {
            chatTargetEmail.innerText = this.options[this.selectedIndex].text;
        }
        currentMessageCount = 0; // Reset count when changing customers
        loadMessages();
    });
}

if (chatForm) {
    chatForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        const text = (chatMessage.value || '').trim();
        if (!text) return;
        chatMessage.value = '';
        await sendMessage(text);
        await loadMessages();
    });
}

loadMessages();
setInterval(loadMessages, 2000);

async function checkUnreadStatus() {
    if (!customerSelect) return;

    try {
        const res = await fetch('/api/admin/chat/unread-status');
        const data = await res.json();

        if (data.success && data.unreadCustomerIds) {
            const selectedId = customerSelect.value;
            let unreadIds = Array.isArray(data.unreadCustomerIds) ? data.unreadCustomerIds.map(String) : [];

            Array.from(customerSelect.options).forEach(opt => {
                if (!opt.value) return; // Skip placeholder

                // If it's a customer with unread msg AND not currently selected
                let hasUnread = unreadIds.includes(opt.value) && opt.value !== selectedId;

                // Remove existing dot if present
                let cleanText = opt.text.replace(' •', '');

                // Add dot if unread
                opt.text = hasUnread ? cleanText + ' •' : cleanText;
            });
        }
    } catch (err) {
        console.error("Error fetching unread status:", err);
    }
}

// Check unread status once when the page loads
checkUnreadStatus();

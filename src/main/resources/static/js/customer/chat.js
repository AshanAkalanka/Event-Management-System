const chatBox = document.getElementById('chatBox');
const chatForm = document.getElementById('chatForm');
const chatMessage = document.getElementById('chatMessage');

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
        // Customer sends -> 'sent', Admin sends -> 'received'
        const isSelf = m.senderRole === 'USER';
        div.className = 'msg ' + (isSelf ? 'sent' : 'received');
        div.innerHTML = escapeHtml(m.message) + '<span class="msg-time">' + (m.createdAt || '') + '</span>';
        chatBox.appendChild(div);
    });

    chatBox.scrollTop = chatBox.scrollHeight;
    currentMessageCount = messages.length;
}

async function loadMessages() {
    const res = await fetch('/api/chat/messages');
    const data = await res.json();
    if (data.success) {
        renderMessages(data.messages || []);
    }
}

async function sendMessage(text) {
    const formData = new FormData();
    formData.append('message', text);

    const res = await fetch('/api/chat/messages', {
        method: 'POST',
        body: formData
    });

    const data = await res.json();
    if (!data.success) {
        alert(data.message || 'Failed');
    }
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

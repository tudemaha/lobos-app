function openCreateModal() {
    const btn = document.getElementById('btn-new-collection');
    if (btn && btn.getAttribute('data-quota-reached') === 'true') {
        sendQuotaInteraction('CLICK_ADD_COLLECTION');
        document.getElementById('modal-quota').classList.remove('hidden');
        return;
    }
    document.getElementById('modal-create').classList.remove('hidden');
}
function closeCreateModal() {
    document.getElementById('modal-create').classList.add('hidden');
}
function closeQuotaModal() {
    document.getElementById('modal-quota').classList.add('hidden');
}
function sendQuotaInteraction(action) {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    if (tokenMeta && headerMeta) {
        headers[headerMeta.getAttribute('content')] = tokenMeta.getAttribute('content');
    }
    return fetch('/collections/quota-interaction', {
        method: 'POST',
        headers: headers,
        body: new URLSearchParams({ action: action })
    });
}
function notifyDeveloperForQuota() {
    const btn = document.getElementById('btn-notify-developer');
    if (btn) btn.disabled = true;
    sendQuotaInteraction('NOTIFY_DEVELOPER')
        .then(function(res) {
            if (res.ok) {
                document.getElementById('quota-notify-section').classList.add('hidden');
                document.getElementById('quota-notified-msg').classList.remove('hidden');
            } else if (btn) {
                btn.disabled = false;
            }
        })
        .catch(function() {
            if (btn) btn.disabled = false;
        });
}
function openEditModal(btn) {
    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');
    const color = btn.getAttribute('data-color');
    document.getElementById('edit-col-id').value = id;
    document.getElementById('edit-col-name').value = name;
    document.getElementById('edit-col-color').value = color;
    document.getElementById('edit-col-color-picker').value = color;
    document.getElementById('edit-collection-form').action = '/collections/' + id;
    document.getElementById('modal-edit').classList.remove('hidden');
}
function closeEditModal() {
    document.getElementById('modal-edit').classList.add('hidden');
}
function confirmDelete(btn) {
    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');
    showConfirmModal('Delete collection "' + name + '"? This will remove all grammars inside it.', function() {
        const form = document.getElementById('delete-collection-form');
        form.action = '/collections/' + id;
        form.submit();
    });
}
function syncColorInput(inputId, value) {
    document.getElementById(inputId).value = value;
}
function syncColorPicker(pickerId, value) {
    if (/^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/.test(value)) {
        document.getElementById(pickerId).value = value;
    }
}
['modal-create', 'modal-edit', 'modal-quota'].forEach(function(id) {
    document.getElementById(id).addEventListener('click', function(e) {
        if (e.target === this) this.classList.add('hidden');
    });
});

function openCreateModal() {
    document.getElementById('modal-create-token').classList.remove('hidden');
}
function closeCreateModal() {
    document.getElementById('modal-create-token').classList.add('hidden');
}
function openEditModal(btn) {
    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');
    document.getElementById('edit-token-name').value = name;
    document.getElementById('edit-token-form').action = '/tokens/' + id;
    document.getElementById('modal-edit-token').classList.remove('hidden');
}
function closeEditModal() {
    document.getElementById('modal-edit-token').classList.add('hidden');
}
function confirmDelete(btn) {
    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');
    showConfirmModal('Revoke token "' + name + '"? Any client using it will immediately lose access.', function() {
        const form = document.getElementById('delete-token-form');
        form.action = '/tokens/' + id;
        form.submit();
    });
}
function copyToken() {
    const el = document.getElementById('new-token-value');
    if (!el) return;

    navigator.clipboard.writeText(el.textContent.trim()).then(function() {
        const btn = document.getElementById('btn-copy-token');
        const feedback = document.getElementById('copy-token-feedback');

        if (btn) {
            btn.textContent = 'Copied!';
            setTimeout(function() {
                btn.textContent = 'Copy';
            }, 2000);
        }
        if (feedback) {
            feedback.classList.remove('hidden');
            setTimeout(function() {
                feedback.classList.add('hidden');
            }, 2000);
        }
    });
}
['modal-create-token', 'modal-edit-token'].forEach(function(id) {
    const el = document.getElementById(id);
    if (el) {
        el.addEventListener('click', function(e) {
            if (e.target === this) this.classList.add('hidden');
        });
    }
});

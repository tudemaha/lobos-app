function toggleDropdown() {
    const dd = document.getElementById('nav-dropdown');
    dd.classList.toggle('hidden');
}
function toggleMobileMenu() {
    const m = document.getElementById('mobile-menu');
    m.classList.toggle('hidden');
}
document.addEventListener('click', function(e) {
    const btn = document.getElementById('nav-profile-btn');
    const dd = document.getElementById('nav-dropdown');
    if (btn && dd && !btn.contains(e.target) && !dd.contains(e.target)) {
        dd.classList.add('hidden');
    }
});
function togglePwd(inputId) {
    const input = document.getElementById(inputId);
    input.type = input.type === 'password' ? 'text' : 'password';
}

let _confirmModalCallback = null;
function showConfirmModal(message, onConfirm) {
    document.getElementById('confirm-modal-message').textContent = message;
    _confirmModalCallback = onConfirm;
    document.getElementById('confirm-modal').classList.remove('hidden');
}
function hideConfirmModal() {
    document.getElementById('confirm-modal').classList.add('hidden');
    _confirmModalCallback = null;
}
document.getElementById('confirm-modal-cancel').addEventListener('click', hideConfirmModal);
document.getElementById('confirm-modal-confirm').addEventListener('click', function() {
    const callback = _confirmModalCallback;
    hideConfirmModal();
    if (callback) callback();
});
document.getElementById('confirm-modal').addEventListener('click', function(e) {
    if (e.target === this) hideConfirmModal();
});

function confirmDeleteAccount() {
    showConfirmModal('Are you sure you want to permanently delete your account? This action cannot be undone.', function() {
        document.getElementById('delete-account-form').submit();
    });
}

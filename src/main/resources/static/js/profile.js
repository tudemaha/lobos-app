function confirmDeleteAccount() {
    if (confirm('Are you sure you want to permanently delete your account? This action cannot be undone.')) {
        document.getElementById('delete-account-form').submit();
    }
}

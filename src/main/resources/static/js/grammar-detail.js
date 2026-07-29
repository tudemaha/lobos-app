function openEditModal() {
    document.getElementById('modal-edit-grammar').classList.remove('hidden');
}
function closeEditModal() {
    document.getElementById('modal-edit-grammar').classList.add('hidden');
}
function confirmDelete() {
    if (confirm('Delete this grammar entry? This action cannot be undone.')) {
        document.getElementById('delete-grammar-form').submit();
    }
}
document.getElementById('modal-edit-grammar').addEventListener('click', function(e) {
    if (e.target === this) this.classList.add('hidden');
});

function openCreateModal() {
    document.getElementById('modal-create-grammar').classList.remove('hidden');
}
function closeCreateModal() {
    document.getElementById('modal-create-grammar').classList.add('hidden');
}
function openEditModal(btn) {
    const id = btn.getAttribute('data-id');
    const word = btn.getAttribute('data-word');
    const meaning = btn.getAttribute('data-meaning');
    document.getElementById('edit-g-word').value = word;
    document.getElementById('edit-g-meaning').value = meaning;
    document.getElementById('edit-g-example').value = '';
    document.getElementById('edit-grammar-form').action = '/collections/' + collectionId + '/grammars/' + id;
    document.getElementById('modal-edit-grammar').classList.remove('hidden');
}
function closeEditModal() {
    document.getElementById('modal-edit-grammar').classList.add('hidden');
}
function confirmDelete(btn) {
    const id = btn.getAttribute('data-id');
    const word = btn.getAttribute('data-word');
    if (confirm('Delete grammar entry "' + word + '"?')) {
        const form = document.getElementById('delete-grammar-form');
        form.action = '/collections/' + collectionId + '/grammars/' + id;
        form.submit();
    }
}
['modal-create-grammar', 'modal-edit-grammar'].forEach(function(id) {
    document.getElementById(id).addEventListener('click', function(e) {
        if (e.target === this) this.classList.add('hidden');
    });
});

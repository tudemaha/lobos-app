function openCreateModal() {
    document.getElementById('modal-create').classList.remove('hidden');
}
function closeCreateModal() {
    document.getElementById('modal-create').classList.add('hidden');
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
['modal-create', 'modal-edit'].forEach(function(id) {
    document.getElementById(id).addEventListener('click', function(e) {
        if (e.target === this) this.classList.add('hidden');
    });
});

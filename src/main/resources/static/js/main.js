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

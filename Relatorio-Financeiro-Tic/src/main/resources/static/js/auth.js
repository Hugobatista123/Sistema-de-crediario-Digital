// Verificação de autenticação ao carregar pages protegidas
(async function verificarAuth() {
    try {
        const response = await fetch('/api/auth/me', { credentials: 'include' });
        if (!response.ok) {
            window.location.href = '/login.html';
            return;
        }
        const usuario = await response.json();
        localStorage.setItem('usuario', JSON.stringify(usuario));
        exibirInfoUsuario(usuario);
    } catch (err) {
        window.location.href = '/login.html';
    }
})();

function exibirInfoUsuario(usuario) {
    const el = document.getElementById('usuarioLogado');
    if (el) el.textContent = usuario.nome + ' (' + usuario.perfil + ')';

    // Exibe nav e seção de logs apenas para ADMIN
    if (usuario.perfil === 'ADMIN') {
        const navLogs = document.getElementById('nav-logs');
        if (navLogs) navLogs.style.display = '';
        const secaoLogs = document.getElementById('logs');
        if (secaoLogs) secaoLogs.style.display = '';
    }
}

async function logout() {
    try {
        await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
    } catch (err) {
        // ignora erro de rede no logout
    } finally {
        localStorage.removeItem('usuario');
        window.location.href = '/login.html';
    }
}

document.getElementById('loginForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const btn = document.getElementById('btnLogin');
    const erroMsg = document.getElementById('erroMsg');
    erroMsg.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Entrando...';

    const login = document.getElementById('login').value.trim();
    const senha = document.getElementById('senha').value;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ login: login, senha: senha }),
            credentials: 'include'
        });

        if (!response.ok) {
            const data = await response.json().catch(function () { return {}; });
            erroMsg.textContent = data.erro || 'Login ou senha invalidos';
            erroMsg.style.display = 'block';
            return;
        }

        const data = await response.json();
        localStorage.setItem('usuario', JSON.stringify({ nome: data.nome, perfil: data.perfil }));
        window.location.href = '/index.html';

    } catch (err) {
        erroMsg.textContent = 'Erro de conexao. Tente novamente.';
        erroMsg.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Entrar';
    }
});

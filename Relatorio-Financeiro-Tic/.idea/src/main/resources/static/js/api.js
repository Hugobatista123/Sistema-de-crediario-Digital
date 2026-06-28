// Funcoes base de fetch para consumir a API REST do backend Spring Boot.
// Padrao obrigatorio (briefing v2.0 secao 1.1): async/await, try/catch,
// verificacao de response.ok e mensagem de erro exibida na interface.

const API_BASE_URL = '';

function exibirMensagemErro(mensagem) {
    const container = document.getElementById('mensagem-erro');
    if (container) {
        container.textContent = mensagem;
        container.classList.add('visivel');
        setTimeout(() => container.classList.remove('visivel'), 6000);
    } else {
        console.warn('Elemento #mensagem-erro nao encontrado. Mensagem:', mensagem);
    }
}

async function processarResposta(response, contexto) {
    if (!response.ok) {
        throw new Error(`Erro ${response.status} (${contexto}): ${response.statusText}`);
    }
    if (response.status === 204) {
        return null;
    }
    return await response.json();
}

async function get(path) {
    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });
        return await processarResposta(response, `GET ${path}`);
    } catch (error) {
        console.error(error);
        exibirMensagemErro(`Falha ao buscar dados: ${error.message}`);
        throw error;
    }
}

async function post(path, body) {
    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(body)
        });
        return await processarResposta(response, `POST ${path}`);
    } catch (error) {
        console.error(error);
        exibirMensagemErro(`Falha ao enviar dados: ${error.message}`);
        throw error;
    }
}

async function put(path, body) {
    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(body)
        });
        return await processarResposta(response, `PUT ${path}`);
    } catch (error) {
        console.error(error);
        exibirMensagemErro(`Falha ao atualizar dados: ${error.message}`);
        throw error;
    }
}

async function del(path) {
    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: 'DELETE',
            headers: { 'Accept': 'application/json' }
        });
        return await processarResposta(response, `DELETE ${path}`);
    } catch (error) {
        console.error(error);
        exibirMensagemErro(`Falha ao excluir registro: ${error.message}`);
        throw error;
    }
}

window.api = { get, post, put, del, exibirMensagemErro };

// Modulo Clientes - Sprint 2
// Depende de js/api.js (window.api)

(function () {

    // Carregar clientes ativos (com busca opcional por nome)
    async function carregarClientes(nome) {
        const path = nome ? '/api/clientes?nome=' + encodeURIComponent(nome) : '/api/clientes';
        try {
            const clientes = await window.api.get(path);
            renderizarClientes(clientes || []);
        } catch (error) {
            document.getElementById('lista-clientes-container').innerHTML = '<p>Erro ao carregar clientes.</p>';
        }
    }

    // Carregar clientes excluidos
    async function carregarExcluidos() {
        try {
            const excluidos = await window.api.get('/api/clientes/excluidos');
            renderizarExcluidos(excluidos || []);
        } catch (error) {
            document.getElementById('lista-excluidos-container').innerHTML = '<p>Erro ao carregar clientes excluidos.</p>';
        }
    }

    // Renderizar lista de clientes ativos com botoes Editar e Excluir
    function renderizarClientes(clientes) {
        const container = document.getElementById('lista-clientes-container');
        if (clientes.length === 0) {
            container.innerHTML = '<p>Nenhum cliente ativo encontrado.</p>';
            return;
        }
        const ul = document.createElement('ul');
        clientes.forEach(function (c) {
            const li = document.createElement('li');
            li.innerHTML =
                '<strong>' + escaparHtml(c.nome) + (c.sobrenome ? ' ' + escaparHtml(c.sobrenome) : '') + '</strong>' +
                ' [' + escaparHtml(c.tipo || '') + ']' +
                (c.telefone ? ' | Tel: ' + escaparHtml(c.telefone) : '') +
                (c.email ? ' | Email: ' + escaparHtml(c.email) : '') +
                (c.cpf ? ' | CPF: ' + escaparHtml(c.cpf) : '') +
                (c.cnpj ? ' | CNPJ: ' + escaparHtml(c.cnpj) : '') +
                (c.razaoSocial ? ' | Razao Social: ' + escaparHtml(c.razaoSocial) : '') +
                ' <button data-id="' + c.id + '" class="btn-editar-cliente">Editar</button>' +
                ' <button data-id="' + c.id + '" class="btn-excluir-cliente">Excluir</button>';
            ul.appendChild(li);
        });
        container.innerHTML = '';
        container.appendChild(ul);

        container.querySelectorAll('.btn-editar-cliente').forEach(function (btn) {
            btn.addEventListener('click', function () {
                const id = Number(btn.getAttribute('data-id'));
                const cliente = clientes.find(function (c) { return c.id === id; });
                if (cliente) {
                    preencherFormParaEdicao(cliente);
                }
            });
        });

        container.querySelectorAll('.btn-excluir-cliente').forEach(function (btn) {
            btn.addEventListener('click', function () {
                const id = Number(btn.getAttribute('data-id'));
                excluirCliente(id);
            });
        });
    }

    // RF-025: Set em memoria de clientes excluidos marcados como importantes (nao persiste no reload)
    var importantesClientes = new Set();

    // Renderizar lista de excluidos com botao Restaurar e checkbox Importante (RF-025)
    function renderizarExcluidos(clientes) {
        const container = document.getElementById('lista-excluidos-container');
        if (clientes.length === 0) {
            container.innerHTML = '<p>Nenhum cliente excluido.</p>';
            return;
        }

        // Importantes vao para o topo
        var importantes = clientes.filter(function (c) { return importantesClientes.has(c.id); });
        var normais = clientes.filter(function (c) { return !importantesClientes.has(c.id); });
        var ordenados = importantes.concat(normais);

        const ul = document.createElement('ul');
        ul.style.listStyle = 'none';
        ul.style.padding = '0';

        ordenados.forEach(function (c) {
            var li = document.createElement('li');
            li.style.marginBottom = '8px';
            li.style.padding = '8px';
            li.style.border = '1px solid #ddd';
            li.style.borderRadius = '4px';

            var isImportante = importantesClientes.has(c.id);
            if (isImportante) {
                li.style.borderLeft = '3px solid #e74c3c';
                li.style.backgroundColor = '#fff5f5';
            }

            // Nome e tipo via textContent
            var strong = document.createElement('strong');
            strong.textContent = c.nome + (c.sobrenome ? ' ' + c.sobrenome : '');
            li.appendChild(strong);
            li.appendChild(document.createTextNode(' [' + (c.tipo || '') + '] '));

            // Checkbox Importante
            var checkboxId = 'chk-imp-cliente-' + c.id;
            var label = document.createElement('label');
            label.htmlFor = checkboxId;
            label.style.marginRight = '8px';
            label.style.cursor = 'pointer';
            label.style.fontSize = '0.85em';

            var checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = checkboxId;
            checkbox.checked = isImportante;
            checkbox.style.marginRight = '3px';
            checkbox.style.cursor = 'pointer';
            label.appendChild(checkbox);
            label.appendChild(document.createTextNode('Importante'));
            li.appendChild(label);

            // Botao Restaurar
            var btnRestaurar = document.createElement('button');
            btnRestaurar.textContent = 'Restaurar';
            btnRestaurar.setAttribute('data-id', String(c.id));
            btnRestaurar.className = 'btn-restaurar-cliente';
            if (isImportante) {
                btnRestaurar.style.fontWeight = 'bold';
                btnRestaurar.style.background = '#c0392b';
                btnRestaurar.style.color = '#fff';
                btnRestaurar.style.border = 'none';
                btnRestaurar.style.borderRadius = '3px';
                btnRestaurar.style.padding = '3px 10px';
                btnRestaurar.style.cursor = 'pointer';
            }
            li.appendChild(btnRestaurar);

            // Evento: marcar/desmarcar como importante
            checkbox.addEventListener('change', function () {
                if (checkbox.checked) {
                    importantesClientes.add(c.id);
                    li.style.borderLeft = '3px solid #e74c3c';
                    li.style.backgroundColor = '#fff5f5';
                    btnRestaurar.style.fontWeight = 'bold';
                    btnRestaurar.style.background = '#c0392b';
                    btnRestaurar.style.color = '#fff';
                    btnRestaurar.style.border = 'none';
                    btnRestaurar.style.borderRadius = '3px';
                    btnRestaurar.style.padding = '3px 10px';
                    btnRestaurar.style.cursor = 'pointer';
                    ul.insertBefore(li, ul.firstChild);
                } else {
                    importantesClientes.delete(c.id);
                    li.style.borderLeft = '';
                    li.style.backgroundColor = '';
                    btnRestaurar.style.fontWeight = '';
                    btnRestaurar.style.background = '';
                    btnRestaurar.style.color = '';
                    btnRestaurar.style.border = '';
                    btnRestaurar.style.borderRadius = '';
                    btnRestaurar.style.padding = '';
                }
            });

            // Evento: restaurar cliente
            btnRestaurar.addEventListener('click', function () {
                restaurarCliente(Number(btnRestaurar.getAttribute('data-id')));
            });

            ul.appendChild(li);
        });

        container.innerHTML = '';
        container.appendChild(ul);
    }

    // Submit do formulario (cadastro e edicao)
    async function salvarCliente(event) {
        event.preventDefault();
        const dados = coletarDadosForm();
        const idEdicao = document.getElementById('cliente-id-edicao').value;

        try {
            if (idEdicao) {
                await window.api.put('/api/clientes/' + idEdicao, dados);
            } else {
                await window.api.post('/api/clientes', dados);
            }
            limparForm();
            carregarClientes();
            carregarExcluidos();
        } catch (error) {
            // Erro ja exibido por window.api
        }
    }

    // Excluir cliente com confirmacao nativa
    async function excluirCliente(id) {
        if (!confirm('Deseja excluir este cliente?')) {
            return;
        }
        try {
            await window.api.del('/api/clientes/' + id);
            carregarClientes();
            carregarExcluidos();
        } catch (error) {
            // Erro ja exibido por window.api
        }
    }

    // Restaurar cliente excluido
    async function restaurarCliente(id) {
        try {
            await window.api.put('/api/clientes/' + id + '/restaurar', {});
            carregarClientes();
            carregarExcluidos();
        } catch (error) {
            // Erro ja exibido por window.api
        }
    }

    // Preencher formulario para edicao
    function preencherFormParaEdicao(cliente) {
        document.getElementById('cliente-id-edicao').value = cliente.id;
        document.getElementById('cliente-tipo').value = cliente.tipo || 'PF';
        document.getElementById('cliente-nome').value = cliente.nome || '';
        document.getElementById('cliente-sobrenome').value = cliente.sobrenome || '';
        document.getElementById('cliente-telefone').value = cliente.telefone || '';
        document.getElementById('cliente-email').value = cliente.email || '';
        document.getElementById('cliente-conta-gov').value = cliente.contaGov || '';
        document.getElementById('cliente-outros').value = cliente.outros || '';
        document.getElementById('cliente-cpf').value = cliente.cpf || '';
        document.getElementById('cliente-cnpj').value = cliente.cnpj || '';
        document.getElementById('cliente-razao-social').value = cliente.razaoSocial || '';
        document.getElementById('btn-salvar-cliente').textContent = 'Atualizar';
        document.getElementById('btn-cancelar-edicao').style.display = '';
        atualizarCamposTipo();
    }

    // Limpar formulario e voltar ao modo cadastro
    function limparForm() {
        document.getElementById('form-cliente').reset();
        document.getElementById('cliente-id-edicao').value = '';
        document.getElementById('btn-salvar-cliente').textContent = 'Cadastrar';
        document.getElementById('btn-cancelar-edicao').style.display = 'none';
        atualizarCamposTipo();
    }

    // Alternar campos PF/PJ conforme tipo selecionado
    function atualizarCamposTipo() {
        const tipo = document.getElementById('cliente-tipo').value;
        const isPF = tipo === 'PF';

        document.getElementById('campo-sobrenome').style.display = isPF ? '' : 'none';
        document.getElementById('campo-cpf').style.display = isPF ? '' : 'none';
        document.getElementById('campo-cnpj').style.display = isPF ? 'none' : '';
        document.getElementById('campo-razao-social').style.display = isPF ? 'none' : '';
    }

    // Coletar dados do formulario para ClienteRequest
    function coletarDadosForm() {
        return {
            tipo: document.getElementById('cliente-tipo').value,
            nome: document.getElementById('cliente-nome').value.trim(),
            sobrenome: document.getElementById('cliente-sobrenome').value.trim() || null,
            telefone: document.getElementById('cliente-telefone').value.trim() || null,
            email: document.getElementById('cliente-email').value.trim() || null,
            contaGov: document.getElementById('cliente-conta-gov').value.trim() || null,
            outros: document.getElementById('cliente-outros').value.trim() || null,
            cpf: document.getElementById('cliente-cpf').value.trim() || null,
            cnpj: document.getElementById('cliente-cnpj').value.trim() || null,
            razaoSocial: document.getElementById('cliente-razao-social').value.trim() || null
        };
    }

    // Utilitario para escapar HTML e evitar XSS
    function escaparHtml(texto) {
        const div = document.createElement('div');
        div.appendChild(document.createTextNode(texto));
        return div.innerHTML;
    }

    // Init
    function init() {
        document.getElementById('cliente-tipo').addEventListener('change', atualizarCamposTipo);
        document.getElementById('form-cliente').addEventListener('submit', salvarCliente);
        document.getElementById('btn-cancelar-edicao').addEventListener('click', limparForm);

        var debounceTimer;
        document.getElementById('busca-nome-cliente').addEventListener('input', function () {
            clearTimeout(debounceTimer);
            var termo = this.value.trim();
            debounceTimer = setTimeout(function () {
                carregarClientes(termo);
            }, 300);
        });

        atualizarCamposTipo();
        carregarClientes();
        carregarExcluidos();
    }

    document.addEventListener('DOMContentLoaded', init);
})();

// Modulo Servicos - Sprint 3
// Depende de js/api.js (window.api)

(function () {

    var clienteAtualId = null;

    function exibirErro(msg) {
        var el = document.getElementById('mensagem-erro');
        if (el) {
            el.textContent = msg;
            setTimeout(function () { el.textContent = ''; }, 5000);
        }
    }

    function escaparHtml(texto) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(String(texto)));
        return div.innerHTML;
    }

    function badgeStatus(status) {
        var estilos = {
            'A_COBRAR': 'background:#6c757d;color:#fff;',
            'PENDENTE': 'background:#ffc107;color:#212529;',
            'PAGO': 'background:#28a745;color:#fff;'
        };
        var estilo = estilos[status] || '';
        return '<span style="' + estilo + 'padding:2px 8px;border-radius:4px;font-size:0.85em;">'
            + escaparHtml(status) + '</span>';
    }

    function proximoStatusValido(statusAtual) {
        if (statusAtual === 'A_COBRAR') return 'PENDENTE';
        if (statusAtual === 'PENDENTE') return 'PAGO';
        return null;
    }

    async function carregarServicos(clienteId) {
        clienteAtualId = clienteId;
        try {
            const response = await fetch('/api/clientes/' + clienteId + '/servicos');
            if (!response.ok) throw new Error('Erro ' + response.status);
            const servicos = await response.json();
            renderizarServicos(servicos || []);
        } catch (error) {
            console.error('Erro ao carregar servicos:', error);
            exibirErro('Nao foi possivel carregar os servicos do cliente.');
        }
    }

    function renderizarServicos(servicos) {
        var container = document.getElementById('lista-servicos-container');
        if (!container) return;
        if (servicos.length === 0) {
            container.innerHTML = '<p>Nenhum servico ativo para este cliente.</p>';
            return;
        }
        var ul = document.createElement('ul');
        ul.style.listStyle = 'none';
        ul.style.padding = '0';
        servicos.forEach(function (s) {
            var li = document.createElement('li');
            li.style.marginBottom = '12px';
            li.style.padding = '10px';
            li.style.border = '1px solid #ddd';
            li.style.borderRadius = '4px';

            var proximoStatus = proximoStatusValido(s.status);

            // Linha 1: descricao + badge status
            var linhaDescricao = document.createElement('div');
            var strongDescricao = document.createElement('strong');
            strongDescricao.textContent = s.descricao;
            linhaDescricao.appendChild(strongDescricao);
            linhaDescricao.appendChild(document.createTextNode(' '));

            // Badge status via innerHTML seguro (sem dados da API no HTML)
            var badgeSpan = document.createElement('span');
            var estilosBadge = {
                'A_COBRAR': 'background:#6c757d;color:#fff;',
                'PENDENTE': 'background:#ffc107;color:#212529;',
                'PAGO': 'background:#28a745;color:#fff;'
            };
            badgeSpan.setAttribute('style', (estilosBadge[s.status] || '') + 'padding:2px 8px;border-radius:4px;font-size:0.85em;');
            badgeSpan.textContent = s.status;
            linhaDescricao.appendChild(badgeSpan);
            li.appendChild(linhaDescricao);

            // Linha 2: data, valor, saldo, categoria
            var linhaInfo = document.createElement('div');
            var infoTexto = 'Data: ' + s.data
                + ' | Valor: R$ ' + Number(s.valor).toFixed(2)
                + ' | Saldo Devedor: ';
            linhaInfo.appendChild(document.createTextNode(infoTexto));
            var saldoStrong = document.createElement('strong');
            saldoStrong.textContent = 'R$ ' + Number(s.saldoDevedor).toFixed(2);
            linhaInfo.appendChild(saldoStrong);
            if (s.categoriaNome) {
                linhaInfo.appendChild(document.createTextNode(' | Categoria: ' + s.categoriaNome));
            }
            li.appendChild(linhaInfo);

            // Linha 3: botoes de acao
            var linhaAcoes = document.createElement('div');
            linhaAcoes.style.marginTop = '6px';

            if (proximoStatus) {
                var btnStatusEl = document.createElement('button');
                btnStatusEl.textContent = 'Mover para ' + proximoStatus;
                btnStatusEl.className = 'btn-alterar-status';
                btnStatusEl.setAttribute('data-id', String(s.id));
                btnStatusEl.setAttribute('data-status', proximoStatus);
                (function (id, status) {
                    btnStatusEl.addEventListener('click', function () {
                        alterarStatus(id, status);
                    });
                }(s.id, proximoStatus));
                linhaAcoes.appendChild(btnStatusEl);
                linhaAcoes.appendChild(document.createTextNode(' '));
            }

            if (s.status === 'PENDENTE') {
                var btnPagEl = document.createElement('button');
                btnPagEl.textContent = 'Registrar Pagamento';
                btnPagEl.className = 'btn-abrir-pagamento';
                btnPagEl.setAttribute('data-id', String(s.id));
                (function (id) {
                    btnPagEl.addEventListener('click', function () {
                        abrirFormPagamento(id);
                    });
                }(s.id));
                linhaAcoes.appendChild(btnPagEl);
                linhaAcoes.appendChild(document.createTextNode(' '));
            }

            var btnExcluirEl = document.createElement('button');
            btnExcluirEl.textContent = 'Excluir';
            btnExcluirEl.className = 'btn-excluir-servico';
            btnExcluirEl.setAttribute('data-id', String(s.id));
            (function (id) {
                btnExcluirEl.addEventListener('click', function () {
                    excluirServico(id);
                });
            }(s.id));
            linhaAcoes.appendChild(btnExcluirEl);

            // RF-027: Botao Ver Pagamentos (colapsavel)
            var btnVerPag = document.createElement('button');
            btnVerPag.textContent = 'Ver Pagamentos';
            btnVerPag.style.marginLeft = '6px';
            btnVerPag.style.fontSize = '0.85em';
            linhaAcoes.appendChild(btnVerPag);

            li.appendChild(linhaAcoes);

            // RF-027: Container colapsavel de historico de pagamentos
            var historicoContainer = document.createElement('div');
            historicoContainer.style.display = 'none';
            historicoContainer.style.marginTop = '8px';
            historicoContainer.style.paddingLeft = '12px';
            historicoContainer.style.borderLeft = '2px solid #ddd';
            historicoContainer.textContent = 'Carregando...';
            li.appendChild(historicoContainer);

            var historicoCarregado = false;
            btnVerPag.addEventListener('click', function () {
                if (historicoContainer.style.display === 'none') {
                    historicoContainer.style.display = 'block';
                    btnVerPag.textContent = 'Ocultar Pagamentos';
                    if (!historicoCarregado) {
                        historicoCarregado = true;
                        fetch('/api/servicos/' + s.id + '/pagamentos')
                            .then(function (r) {
                                if (!r.ok) throw new Error('Erro ' + r.status);
                                return r.json();
                            })
                            .then(function (pagamentos) {
                                historicoContainer.textContent = '';
                                if (!pagamentos || pagamentos.length === 0) {
                                    historicoContainer.textContent = 'Nenhum pagamento registrado.';
                                    return;
                                }
                                var ulPag = document.createElement('ul');
                                ulPag.style.margin = '4px 0';
                                ulPag.style.paddingLeft = '16px';
                                pagamentos.forEach(function (p) {
                                    var liPag = document.createElement('li');
                                    liPag.textContent = 'Data: ' + p.dataPagamento
                                        + ' | Valor Pago: R$ ' + Number(p.valorPago).toFixed(2);
                                    ulPag.appendChild(liPag);
                                });
                                historicoContainer.appendChild(ulPag);
                            })
                            .catch(function () {
                                historicoContainer.textContent = 'Erro ao carregar pagamentos.';
                            });
                    }
                } else {
                    historicoContainer.style.display = 'none';
                    btnVerPag.textContent = 'Ver Pagamentos';
                }
            });

            ul.appendChild(li);
        });
        container.innerHTML = '';
        container.appendChild(ul);
    }

    async function registrarServico(event) {
        event.preventDefault();
        if (!clienteAtualId) {
            exibirErro('Selecione um cliente antes de registrar um servico.');
            return;
        }
        var descricao = document.getElementById('servico-descricao').value.trim();
        var data = document.getElementById('servico-data').value;
        var valor = document.getElementById('servico-valor').value;
        var categoriaId = document.getElementById('servico-categoria').value;

        if (!descricao || !data || !valor || !categoriaId) {
            exibirErro('Preencha todos os campos obrigatorios do servico.');
            return;
        }

        try {
            const response = await fetch('/api/clientes/' + clienteAtualId + '/servicos', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    descricao: descricao,
                    data: data,
                    valor: parseFloat(valor),
                    categoriaId: parseInt(categoriaId)
                })
            });
            if (!response.ok) {
                var err = await response.json().catch(function () { return {}; });
                throw new Error(err.erro || 'Erro ' + response.status);
            }
            document.getElementById('form-servico').reset();
            carregarServicos(clienteAtualId);
        } catch (error) {
            console.error('Erro ao registrar servico:', error);
            exibirErro('Erro ao registrar servico: ' + error.message);
        }
    }

    async function alterarStatus(id, novoStatus) {
        try {
            const response = await fetch('/api/servicos/' + id + '/status', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ novoStatus: novoStatus })
            });
            if (!response.ok) {
                var err = await response.json().catch(function () { return {}; });
                throw new Error(err.erro || 'Erro ' + response.status);
            }
            carregarServicos(clienteAtualId);
        } catch (error) {
            console.error('Erro ao alterar status:', error);
            exibirErro('Erro ao alterar status: ' + error.message);
        }
    }

    async function excluirServico(id) {
        if (!confirm('Deseja excluir este servico?')) return;
        try {
            const response = await fetch('/api/servicos/' + id, { method: 'DELETE' });
            if (!response.ok && response.status !== 204) {
                var err = await response.json().catch(function () { return {}; });
                throw new Error(err.erro || 'Erro ' + response.status);
            }
            carregarServicos(clienteAtualId);
            carregarExcluidos();
        } catch (error) {
            console.error('Erro ao excluir servico:', error);
            exibirErro('Erro ao excluir servico: ' + error.message);
        }
    }

    async function carregarExcluidos() {
        try {
            const response = await fetch('/api/servicos/excluidos');
            if (!response.ok) throw new Error('Erro ' + response.status);
            const excluidos = await response.json();
            renderizarExcluidos(excluidos || []);
        } catch (error) {
            console.error('Erro ao carregar excluidos:', error);
            exibirErro('Nao foi possivel carregar os servicos excluidos.');
        }
    }

    // RF-025: Set em memoria de servicos excluidos marcados como importantes (nao persiste no reload)
    var importantesServicos = new Set();

    function renderizarExcluidos(servicos) {
        var container = document.getElementById('lista-servicos-excluidos-container');
        if (!container) return;
        if (servicos.length === 0) {
            container.innerHTML = '<p>Nenhum servico excluido.</p>';
            return;
        }

        // Importantes vao para o topo
        var importantes = servicos.filter(function (s) { return importantesServicos.has(s.id); });
        var normais = servicos.filter(function (s) { return !importantesServicos.has(s.id); });
        var ordenados = importantes.concat(normais);

        var ul = document.createElement('ul');
        ul.style.listStyle = 'none';
        ul.style.padding = '0';

        ordenados.forEach(function (s) {
            var li = document.createElement('li');
            li.style.marginBottom = '8px';
            li.style.padding = '8px';
            li.style.border = '1px solid #ddd';
            li.style.borderRadius = '4px';

            var isImportante = importantesServicos.has(s.id);
            if (isImportante) {
                li.style.borderLeft = '3px solid #e74c3c';
                li.style.backgroundColor = '#fff5f5';
            }

            // Descricao e cliente via textContent
            var strong = document.createElement('strong');
            strong.textContent = s.descricao;
            li.appendChild(strong);
            li.appendChild(document.createTextNode(' | Cliente ID: ' + s.clienteId + ' '));

            // Checkbox Importante
            var checkboxId = 'chk-imp-servico-' + s.id;
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
            btnRestaurar.setAttribute('data-id', String(s.id));
            btnRestaurar.className = 'btn-restaurar-servico';
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
                    importantesServicos.add(s.id);
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
                    importantesServicos.delete(s.id);
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

            // Evento: restaurar servico
            btnRestaurar.addEventListener('click', function () {
                restaurarServico(Number(btnRestaurar.getAttribute('data-id')));
            });

            ul.appendChild(li);
        });

        container.innerHTML = '';
        container.appendChild(ul);
    }

    async function restaurarServico(id) {
        try {
            const response = await fetch('/api/servicos/' + id + '/restaurar', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: '{}'
            });
            if (!response.ok) {
                var err = await response.json().catch(function () { return {}; });
                throw new Error(err.erro || 'Erro ' + response.status);
            }
            carregarExcluidos();
            if (clienteAtualId) carregarServicos(clienteAtualId);
        } catch (error) {
            console.error('Erro ao restaurar servico:', error);
            exibirErro('Erro ao restaurar servico: ' + error.message);
        }
    }

    async function carregarCategoriasNoSelect() {
        var select = document.getElementById('servico-categoria');
        if (!select) return;
        try {
            const response = await fetch('/api/categorias');
            if (!response.ok) throw new Error('Erro ' + response.status);
            const categorias = await response.json();
            select.innerHTML = '<option value="">Selecione uma categoria</option>';
            (categorias || []).forEach(function (c) {
                var opt = document.createElement('option');
                opt.value = c.id;
                opt.textContent = c.nome;
                select.appendChild(opt);
            });
        } catch (error) {
            console.error('Erro ao carregar categorias:', error);
            exibirErro('Nao foi possivel carregar as categorias.');
        }
    }

    function abrirFormPagamento(servicoId) {
        var container = document.getElementById('form-pagamento-container');
        if (container) {
            container.style.display = '';
            container.setAttribute('data-servico-id', servicoId);
            var label = document.getElementById('pagamento-servico-label');
            if (label) label.textContent = 'Servico #' + servicoId;
        }
    }

    function init() {
        var formServico = document.getElementById('form-servico');
        if (formServico) {
            formServico.addEventListener('submit', registrarServico);
        }
        carregarCategoriasNoSelect();
        carregarExcluidos();
    }

    document.addEventListener('DOMContentLoaded', init);

    window.servicos = {
        carregarServicos: carregarServicos
    };
})();

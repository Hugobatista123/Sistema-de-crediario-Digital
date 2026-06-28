// Modulo Relatorios - Sprint 4

(function () {

    function exibirErro(msg) {
        var el = document.getElementById('mensagem-erro');
        if (el) {
            el.textContent = msg;
            el.style.display = 'block';
            setTimeout(function () { el.style.display = 'none'; }, 6000);
        }
    }

    function escaparHtml(texto) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(String(texto || '')));
        return div.innerHTML;
    }

    function formatarReais(valor) {
        return 'R$ ' + Number(valor || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    // ── Relatório de Faturamento ──────────────────────────────────────────────

    async function gerarFaturamento(event) {
        event.preventDefault();
        var inicio = document.getElementById('fat-inicio').value;
        var fim = document.getElementById('fat-fim').value;

        if (!inicio || !fim) {
            exibirErro('Informe as datas de inicio e fim.');
            return;
        }
        if (inicio > fim) {
            exibirErro('A data de inicio nao pode ser posterior a data de fim.');
            return;
        }

        var container = document.getElementById('resultado-faturamento');
        if (container) container.innerHTML = '<p>Carregando...</p>';

        try {
            var response = await fetch('/api/relatorios/faturamento?inicio=' + inicio + '&fim=' + fim);
            if (!response.ok) {
                var err = await response.json().catch(function () { return {}; });
                throw new Error(err.erro || 'Erro ' + response.status);
            }
            var dados = await response.json();
            renderizarFaturamento(dados);
        } catch (error) {
            console.error('Erro ao gerar faturamento:', error);
            exibirErro('Erro ao gerar relatorio: ' + error.message);
            if (container) container.innerHTML = '';
        }
    }

    function renderizarFaturamento(dados) {
        var container = document.getElementById('resultado-faturamento');
        if (!container) return;

        if (!dados.itens || dados.itens.length === 0) {
            container.innerHTML = '<p>Nenhum servico pago encontrado no periodo informado.</p>';
            return;
        }

        var linhas = dados.itens.map(function (item) {
            return '<tr>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;">' + escaparHtml(item.nomeCliente) + '</td>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;">' + escaparHtml(item.descricao) + '</td>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;">' + escaparHtml(item.data) + '</td>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;text-align:right;">' + formatarReais(item.valor) + '</td>' +
                '</tr>';
        }).join('');

        container.innerHTML =
            '<table style="width:100%;border-collapse:collapse;margin-top:10px;">' +
            '<thead><tr style="background:#f8f9fa;">' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Cliente</th>' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Descricao</th>' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Data</th>' +
            '<th style="text-align:right;padding:8px;border-bottom:2px solid #dee2e6;">Valor</th>' +
            '</tr></thead>' +
            '<tbody>' + linhas + '</tbody>' +
            '<tfoot><tr style="font-weight:bold;background:#e9f5e9;">' +
            '<td colspan="3" style="padding:8px;border-top:2px solid #dee2e6;">Total Geral</td>' +
            '<td style="padding:8px;border-top:2px solid #dee2e6;text-align:right;">' + formatarReais(dados.totalGeral) + '</td>' +
            '</tr></tfoot>' +
            '</table>';
    }

    // ── Histórico por Cliente ────────────────────────────────────────────────

    async function buscarHistoricoCliente(event) {
        event.preventDefault();
        var clienteId = document.getElementById('hist-cliente-id').value;
        if (!clienteId) {
            exibirErro('Informe o ID do cliente.');
            return;
        }

        var container = document.getElementById('resultado-historico');
        if (container) container.innerHTML = '<p>Carregando...</p>';

        try {
            var response = await fetch('/api/relatorios/cliente/' + clienteId);
            if (!response.ok) {
                var err = await response.json().catch(function () { return {}; });
                throw new Error(err.erro || 'Erro ' + response.status);
            }
            var dados = await response.json();
            renderizarHistorico(dados);
        } catch (error) {
            console.error('Erro ao buscar historico:', error);
            exibirErro('Erro ao buscar historico: ' + error.message);
            if (container) container.innerHTML = '';
        }
    }

    function renderizarHistorico(dados) {
        var container = document.getElementById('resultado-historico');
        if (!container) return;

        var c = dados.dadosCliente || {};
        var servicos = dados.servicos || [];

        var badges = { 'A_COBRAR': '#6c757d', 'PENDENTE': '#ffc107', 'PAGO': '#28a745' };
        var cores = { 'A_COBRAR': '#fff', 'PENDENTE': '#212529', 'PAGO': '#fff' };

        var linhasServicos = servicos.map(function (s) {
            var cor = badges[s.status] || '#999';
            var texto = cores[s.status] || '#fff';
            var badge = '<span style="background:' + cor + ';color:' + texto + ';padding:1px 6px;border-radius:4px;font-size:0.8em;">' + s.status + '</span>';
            var pagamentosHtml = '';
            if (s.pagamentos && s.pagamentos.length > 0) {
                pagamentosHtml = '<ul style="margin:4px 0 0 16px;font-size:0.9em;">' +
                    s.pagamentos.map(function (p) {
                        return '<li>' + escaparHtml(p.dataPagamento) + ': ' + formatarReais(p.valorPago) + '</li>';
                    }).join('') + '</ul>';
            }
            return '<tr>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;">' + escaparHtml(s.descricao) + '</td>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;">' + escaparHtml(s.data) + '</td>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;text-align:right;">' + formatarReais(s.valor) + '</td>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;">' + badge + '</td>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;text-align:right;color:#dc3545;">' + formatarReais(s.saldoDevedor) + '</td>' +
                '<td style="padding:6px 8px;border-bottom:1px solid #dee2e6;">' + pagamentosHtml + '</td>' +
                '</tr>';
        }).join('');

        var cabecalhoCliente = '<div style="margin-bottom:12px;padding:10px;background:#f8f9fa;border-radius:4px;">' +
            '<strong>' + escaparHtml(c.nome) + (c.sobrenome ? ' ' + escaparHtml(c.sobrenome) : '') + '</strong>' +
            ' [' + escaparHtml(c.tipo || '') + ']' +
            (c.email ? ' | ' + escaparHtml(c.email) : '') +
            (c.telefone ? ' | ' + escaparHtml(c.telefone) : '') +
            '</div>';

        if (servicos.length === 0) {
            container.innerHTML = cabecalhoCliente + '<p>Nenhum servico encontrado para este cliente.</p>';
            return;
        }

        container.innerHTML = cabecalhoCliente +
            '<table style="width:100%;border-collapse:collapse;">' +
            '<thead><tr style="background:#f8f9fa;">' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Descricao</th>' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Data</th>' +
            '<th style="text-align:right;padding:8px;border-bottom:2px solid #dee2e6;">Valor</th>' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Status</th>' +
            '<th style="text-align:right;padding:8px;border-bottom:2px solid #dee2e6;">Saldo</th>' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Pagamentos</th>' +
            '</tr></thead>' +
            '<tbody>' + linhasServicos + '</tbody>' +
            '</table>';
    }

    function init() {
        var formFat = document.getElementById('form-faturamento');
        if (formFat) formFat.addEventListener('submit', gerarFaturamento);

        var formHist = document.getElementById('form-historico-cliente');
        if (formHist) formHist.addEventListener('submit', buscarHistoricoCliente);
    }

    document.addEventListener('DOMContentLoaded', init);
})();

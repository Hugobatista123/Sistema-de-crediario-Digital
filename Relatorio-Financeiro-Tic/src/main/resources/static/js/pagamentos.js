// Modulo Pagamentos - Sprint 3
// Depende de js/api.js (window.api) e js/servicos.js (window.servicos)

(function () {

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

    async function carregarPagamentos(servicoId) {
        try {
            const response = await fetch('/api/servicos/' + servicoId + '/pagamentos');
            if (!response.ok) throw new Error('Erro ' + response.status);
            const pagamentos = await response.json();
            renderizarPagamentos(pagamentos || []);
        } catch (error) {
            console.error('Erro ao carregar pagamentos:', error);
            exibirErro('Nao foi possivel carregar os pagamentos do servico.');
        }
    }

    function renderizarPagamentos(pagamentos) {
        var container = document.getElementById('lista-pagamentos-container');
        if (!container) return;
        if (pagamentos.length === 0) {
            container.innerHTML = '<p>Nenhum pagamento registrado.</p>';
            return;
        }
        var ul = document.createElement('ul');
        pagamentos.forEach(function (p) {
            var li = document.createElement('li');
            li.innerHTML = 'Data: ' + escaparHtml(p.dataPagamento)
                + ' | Valor Pago: R$ ' + escaparHtml(Number(p.valorPago).toFixed(2));
            ul.appendChild(li);
        });
        container.innerHTML = '';
        container.appendChild(ul);
    }

    async function registrarPagamento(event) {
        event.preventDefault();
        var container = document.getElementById('form-pagamento-container');
        if (!container) return;
        var servicoId = container.getAttribute('data-servico-id');
        if (!servicoId) {
            exibirErro('Selecione um servico PENDENTE antes de registrar pagamento.');
            return;
        }

        var valorPago = document.getElementById('pagamento-valor').value;
        var dataPagamento = document.getElementById('pagamento-data').value;

        if (!valorPago || !dataPagamento) {
            exibirErro('Preencha o valor e a data do pagamento.');
            return;
        }

        try {
            const response = await fetch('/api/servicos/' + servicoId + '/pagamentos', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    valorPago: parseFloat(valorPago),
                    dataPagamento: dataPagamento
                })
            });
            if (!response.ok) {
                var err = await response.json().catch(function () { return {}; });
                throw new Error(err.erro || 'Erro ' + response.status);
            }
            document.getElementById('form-pagamento').reset();
            carregarPagamentos(Number(servicoId));
            if (window.servicos && window.servicos.carregarServicos) {
                var clienteIdEl = document.getElementById('servicos-cliente-id');
                var clienteId = clienteIdEl ? Number(clienteIdEl.value) : null;
                if (clienteId) window.servicos.carregarServicos(clienteId);
            }
        } catch (error) {
            console.error('Erro ao registrar pagamento:', error);
            exibirErro('Erro ao registrar pagamento: ' + error.message);
        }
    }

    function init() {
        var formPagamento = document.getElementById('form-pagamento');
        if (formPagamento) {
            formPagamento.addEventListener('submit', registrarPagamento);
        }
    }

    document.addEventListener('DOMContentLoaded', init);

    window.pagamentos = {
        carregarPagamentos: carregarPagamentos
    };
})();

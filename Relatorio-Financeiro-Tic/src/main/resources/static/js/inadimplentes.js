// Modulo Inadimplentes - Sprint 4

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

    async function carregarInadimplentes() {
        try {
            var response = await fetch('/api/inadimplentes');
            if (!response.ok) throw new Error('Erro ' + response.status);
            var lista = await response.json();
            renderizarInadimplentes(lista || []);
        } catch (error) {
            console.error('Erro ao carregar inadimplentes:', error);
            exibirErro('Nao foi possivel carregar a lista de inadimplentes.');
        }
    }

    function renderizarInadimplentes(lista) {
        var container = document.getElementById('tabela-inadimplentes-container');
        if (!container) return;

        if (lista.length === 0) {
            container.innerHTML = '<p>Nenhum cliente inadimplente no momento.</p>';
            return;
        }

        var badge = '<span style="background:#ffc107;color:#212529;padding:2px 8px;border-radius:4px;font-size:0.82em;">PENDENTE</span>';

        var linhas = lista.map(function (c) {
            return '<tr>' +
                '<td>' + escaparHtml(c.nome) + ' ' + badge + '</td>' +
                '<td>' + escaparHtml(c.tipo || '') + '</td>' +
                '<td style="font-weight:bold;color:#dc3545;">' + formatarReais(c.totalDevido) + '</td>' +
                '</tr>';
        }).join('');

        container.innerHTML =
            '<table style="width:100%;border-collapse:collapse;">' +
            '<thead><tr style="background:#f8f9fa;">' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Cliente</th>' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Tipo</th>' +
            '<th style="text-align:left;padding:8px;border-bottom:2px solid #dee2e6;">Total Devido</th>' +
            '</tr></thead>' +
            '<tbody>' + linhas + '</tbody>' +
            '</table>';
    }

    function init() {
        carregarInadimplentes();
    }

    document.addEventListener('DOMContentLoaded', init);

    window.inadimplentes = { carregarInadimplentes: carregarInadimplentes };
})();

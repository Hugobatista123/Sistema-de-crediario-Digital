// Dashboard - Sprint 4
// Carregado automaticamente ao abrir index.html

(function () {

    function exibirErro(msg) {
        var el = document.getElementById('mensagem-erro');
        if (el) {
            el.textContent = msg;
            el.style.display = 'block';
            setTimeout(function () { el.style.display = 'none'; }, 6000);
        }
    }

    function formatarReais(valor) {
        return 'R$ ' + Number(valor || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function renderizarCard(classe, titulo, valor, sub) {
        return '<div class="card ' + classe + '">' +
            '  <div class="card-titulo">' + titulo + '</div>' +
            '  <div class="card-valor">' + valor + '</div>' +
            '  <div class="card-sub">' + sub + '</div>' +
            '</div>';
    }

    function renderizarDashboard(dados) {
        var container = document.getElementById('dashboard-cards');
        if (!container) return;

        var cardACobrar, cardPendente, cardPago, cardInadimplentes;

        if (dados.aCobrar) {
            var aCobrar = dados.aCobrar;
            cardACobrar = renderizarCard('card-cinza', 'A Cobrar',
                formatarReais(aCobrar.total), (aCobrar.quantidade || 0) + ' servico(s)');
        } else {
            cardACobrar = renderizarCard('card-cinza', 'A Cobrar', 'Erro ao carregar', '');
        }

        if (dados.pendente) {
            var pendente = dados.pendente;
            cardPendente = renderizarCard('card-amarelo', 'Pendente',
                formatarReais(pendente.total), (pendente.quantidade || 0) + ' servico(s)');
        } else {
            cardPendente = renderizarCard('card-amarelo', 'Pendente', 'Erro ao carregar', '');
        }

        if (dados.pago) {
            var pago = dados.pago;
            var totalPagoMes = pago.totalGeral !== undefined ? pago.totalGeral : 0;
            var qtdPagoMes = pago.itens ? pago.itens.length : 0;
            cardPago = renderizarCard('card-verde', 'Recebido (mes atual)',
                formatarReais(totalPagoMes), qtdPagoMes + ' servico(s)');
        } else {
            cardPago = renderizarCard('card-verde', 'Recebido (mes atual)', 'Erro ao carregar', '');
        }

        if (dados.inadimplentes) {
            var inadimplentes = dados.inadimplentes;
            cardInadimplentes = renderizarCard('card-vermelho', 'Inadimplentes',
                (inadimplentes.quantidade || 0), 'clientes com saldo em aberto');
        } else {
            cardInadimplentes = renderizarCard('card-vermelho', 'Inadimplentes', 'Erro ao carregar', '');
        }

        container.innerHTML = cardACobrar + cardPendente + cardPago + cardInadimplentes;
    }

    function resolverOuErro(resultado) {
        return resultado.status === 'fulfilled' ? resultado.value : null;
    }

    async function carregarDashboard() {
        var agora = new Date();
        var anoMes = agora.getFullYear() + '-' + String(agora.getMonth() + 1).padStart(2, '0');
        var iniciomes = anoMes + '-01';
        var ultimoDia = new Date(agora.getFullYear(), agora.getMonth() + 1, 0).getDate();
        var fimMes = anoMes + '-' + String(ultimoDia).padStart(2, '0');

        var resultados = await Promise.allSettled([
            fetch('/api/relatorios/totais?status=A_COBRAR').then(function (r) {
                if (!r.ok) throw new Error('Erro ' + r.status); return r.json();
            }),
            fetch('/api/relatorios/totais?status=PENDENTE').then(function (r) {
                if (!r.ok) throw new Error('Erro ' + r.status); return r.json();
            }),
            fetch('/api/relatorios/faturamento?inicio=' + iniciomes + '&fim=' + fimMes).then(function (r) {
                if (!r.ok) throw new Error('Erro ' + r.status); return r.json();
            }),
            fetch('/api/inadimplentes/count').then(function (r) {
                if (!r.ok) throw new Error('Erro ' + r.status); return r.json();
            })
        ]);

        var algumFalhou = false;
        resultados.forEach(function (r, i) {
            if (r.status === 'rejected') {
                console.error('Erro no card ' + i + ' do dashboard:', r.reason);
                algumFalhou = true;
            }
        });
        if (algumFalhou) {
            exibirErro('Não foi possível carregar todos os dados do dashboard.');
        }

        renderizarDashboard({
            aCobrar:       resolverOuErro(resultados[0]),
            pendente:      resolverOuErro(resultados[1]),
            pago:          resolverOuErro(resultados[2]),
            inadimplentes: resolverOuErro(resultados[3])
        });
    }

    document.addEventListener('DOMContentLoaded', carregarDashboard);
})();

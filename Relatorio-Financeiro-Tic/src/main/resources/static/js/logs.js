window.logs = (function () {
    async function carregar() {
        const container = document.getElementById('logsContainer');
        if (!container) return;

        try {
            const response = await fetch('/api/logs', { credentials: 'include' });

            if (!response.ok) {
                if (response.status === 401) { window.location.href = '/login.html'; return; }
                if (response.status === 403) {
                    container.innerHTML = '<p>Acesso restrito a administradores.</p>';
                    return;
                }
                throw new Error('Erro ao carregar logs: ' + response.status);
            }

            const logs = await response.json();
            renderizar(logs);

        } catch (err) {
            if (container) container.innerHTML = '<p>Erro ao carregar logs.</p>';
            console.error('Logs:', err);
        }
    }

    function renderizar(logs) {
        const container = document.getElementById('logsContainer');
        if (!logs || logs.length === 0) {
            container.innerHTML = '<p>Nenhum log registrado.</p>';
            return;
        }

        var table = document.createElement('table');
        table.style.cssText = 'width:100%;border-collapse:collapse;';

        var thead = document.createElement('thead');
        var headerRow = document.createElement('tr');
        headerRow.style.background = '#f5f5f5';
        ['Usuario', 'Acao', 'Entidade', 'ID', 'Data/Hora'].forEach(function (h) {
            var th = document.createElement('th');
            th.style.cssText = 'padding:6px;text-align:left;';
            th.textContent = h;
            headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        var tbody = document.createElement('tbody');
        logs.forEach(function (l) {
            var tr = document.createElement('tr');
            var cellStyle = 'padding:5px;border-bottom:1px solid #eee;';
            [
                l.nomeUsuario || '-',
                l.acao || '-',
                l.entidade || '-',
                l.entidadeId != null ? String(l.entidadeId) : '-',
                new Date(l.timestamp).toLocaleString('pt-BR')
            ].forEach(function (val) {
                var td = document.createElement('td');
                td.style.cssText = cellStyle;
                td.textContent = val;
                tr.appendChild(td);
            });
            tbody.appendChild(tr);
        });
        table.appendChild(tbody);

        container.innerHTML = '';
        container.appendChild(table);
    }

    return { carregar: carregar };
})();

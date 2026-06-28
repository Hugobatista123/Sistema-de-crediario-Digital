package br.com.crediario.dto;

import java.util.List;

public class HistoricoClienteResponse {

    private ClienteResponse dadosCliente;
    private List<ServicoComPagamentos> servicos;

    public HistoricoClienteResponse(ClienteResponse dadosCliente, List<ServicoComPagamentos> servicos) {
        this.dadosCliente = dadosCliente;
        this.servicos = servicos;
    }

    public ClienteResponse getDadosCliente() { return dadosCliente; }
    public List<ServicoComPagamentos> getServicos() { return servicos; }
}

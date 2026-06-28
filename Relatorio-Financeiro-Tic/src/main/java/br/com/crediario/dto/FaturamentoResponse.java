package br.com.crediario.dto;

import java.math.BigDecimal;
import java.util.List;

public class FaturamentoResponse {

    private List<ItemFaturamento> itens;
    private BigDecimal totalGeral;

    public FaturamentoResponse(List<ItemFaturamento> itens, BigDecimal totalGeral) {
        this.itens = itens;
        this.totalGeral = totalGeral;
    }

    public List<ItemFaturamento> getItens() { return itens; }
    public BigDecimal getTotalGeral() { return totalGeral; }
}

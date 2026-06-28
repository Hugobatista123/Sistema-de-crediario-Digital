package br.com.crediario.dto;

import java.math.BigDecimal;

public class TotaisStatusResponse {

    private BigDecimal total;
    private int quantidade;

    public TotaisStatusResponse(BigDecimal total, int quantidade) {
        this.total = total;
        this.quantidade = quantidade;
    }

    public BigDecimal getTotal() { return total; }
    public int getQuantidade() { return quantidade; }
}

package br.com.crediario.dto;

import br.com.crediario.model.PagamentoParcial;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PagamentoParcialResponse {

    private Long id;
    private BigDecimal valorPago;
    private LocalDate dataPagamento;

    public static PagamentoParcialResponse fromEntity(PagamentoParcial p) {
        PagamentoParcialResponse r = new PagamentoParcialResponse();
        r.id = p.getId();
        r.valorPago = p.getValorPago();
        r.dataPagamento = p.getDataPagamento();
        return r;
    }

    public Long getId() { return id; }
    public BigDecimal getValorPago() { return valorPago; }
    public LocalDate getDataPagamento() { return dataPagamento; }
}

package br.com.crediario.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PagamentoParcialRequest {

    private BigDecimal valorPago;
    private LocalDate dataPagamento;

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
}

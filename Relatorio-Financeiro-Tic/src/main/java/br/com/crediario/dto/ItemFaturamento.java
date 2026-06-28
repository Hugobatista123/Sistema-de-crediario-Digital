package br.com.crediario.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ItemFaturamento {

    private String nomeCliente;
    private String descricao;
    private LocalDate data;
    private BigDecimal valor;

    public ItemFaturamento(String nomeCliente, String descricao, LocalDate data, BigDecimal valor) {
        this.nomeCliente = nomeCliente;
        this.descricao = descricao;
        this.data = data;
        this.valor = valor;
    }

    public String getNomeCliente() { return nomeCliente; }
    public String getDescricao() { return descricao; }
    public LocalDate getData() { return data; }
    public BigDecimal getValor() { return valor; }
}

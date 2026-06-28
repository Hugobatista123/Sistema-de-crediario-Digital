package br.com.crediario.dto;

import java.math.BigDecimal;

public class ClienteInadimplente {

    private Long id;
    private String nome;
    private String tipo;
    private BigDecimal totalDevido;

    public ClienteInadimplente(Long id, String nome, String tipo, BigDecimal totalDevido) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.totalDevido = totalDevido;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public BigDecimal getTotalDevido() { return totalDevido; }
}

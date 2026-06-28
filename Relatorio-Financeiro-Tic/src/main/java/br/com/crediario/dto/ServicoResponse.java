package br.com.crediario.dto;

import br.com.crediario.model.Servico;
import br.com.crediario.model.StatusCobranca;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ServicoResponse {

    private Long id;
    private String descricao;
    private LocalDate data;
    private BigDecimal valor;
    private StatusCobranca status;
    private boolean ativo;
    private Long clienteId;
    private Long categoriaId;
    private String categoriaNome;
    private BigDecimal saldoDevedor;

    public static ServicoResponse fromEntity(Servico servico, BigDecimal saldoDevedor) {
        ServicoResponse r = new ServicoResponse();
        r.id = servico.getId();
        r.descricao = servico.getDescricao();
        r.data = servico.getData();
        r.valor = servico.getValor();
        r.status = servico.getStatus();
        r.ativo = servico.isAtivo();
        r.clienteId = servico.getCliente().getId();
        r.categoriaId = servico.getCategoria().getId();
        r.categoriaNome = servico.getCategoria().getNome();
        r.saldoDevedor = saldoDevedor;
        return r;
    }

    public Long getId() { return id; }
    public String getDescricao() { return descricao; }
    public LocalDate getData() { return data; }
    public BigDecimal getValor() { return valor; }
    public StatusCobranca getStatus() { return status; }
    public boolean isAtivo() { return ativo; }
    public Long getClienteId() { return clienteId; }
    public Long getCategoriaId() { return categoriaId; }
    public String getCategoriaNome() { return categoriaNome; }
    public BigDecimal getSaldoDevedor() { return saldoDevedor; }
}

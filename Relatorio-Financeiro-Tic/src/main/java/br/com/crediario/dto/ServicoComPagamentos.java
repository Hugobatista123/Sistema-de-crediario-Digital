package br.com.crediario.dto;

import br.com.crediario.model.Servico;
import br.com.crediario.model.StatusCobranca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ServicoComPagamentos {

    private Long id;
    private String descricao;
    private LocalDate data;
    private BigDecimal valor;
    private StatusCobranca status;
    private boolean ativo;
    private Long categoriaId;
    private String categoriaNome;
    private BigDecimal saldoDevedor;
    private List<PagamentoParcialResponse> pagamentos;

    public ServicoComPagamentos(Servico s, List<PagamentoParcialResponse> pagamentos, BigDecimal saldoDevedor) {
        this.id = s.getId();
        this.descricao = s.getDescricao();
        this.data = s.getData();
        this.valor = s.getValor();
        this.status = s.getStatus();
        this.ativo = s.isAtivo();
        this.categoriaId = s.getCategoria().getId();
        this.categoriaNome = s.getCategoria().getNome();
        this.saldoDevedor = saldoDevedor;
        this.pagamentos = pagamentos;
    }

    public Long getId() { return id; }
    public String getDescricao() { return descricao; }
    public LocalDate getData() { return data; }
    public BigDecimal getValor() { return valor; }
    public StatusCobranca getStatus() { return status; }
    public boolean isAtivo() { return ativo; }
    public Long getCategoriaId() { return categoriaId; }
    public String getCategoriaNome() { return categoriaNome; }
    public BigDecimal getSaldoDevedor() { return saldoDevedor; }
    public List<PagamentoParcialResponse> getPagamentos() { return pagamentos; }
}

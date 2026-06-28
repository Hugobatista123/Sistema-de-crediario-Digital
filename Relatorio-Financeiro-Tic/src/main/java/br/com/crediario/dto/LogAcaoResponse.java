package br.com.crediario.dto;

import br.com.crediario.model.LogAcao;
import java.time.LocalDateTime;

public class LogAcaoResponse {
    private Long id;
    private String nomeUsuario;
    private String acao;
    private String entidade;
    private Long entidadeId;
    private LocalDateTime timestamp;

    public static LogAcaoResponse from(LogAcao logAcao) {
        LogAcaoResponse r = new LogAcaoResponse();
        r.id = logAcao.getId();
        r.nomeUsuario = logAcao.getUsuario() != null ? logAcao.getUsuario().getNome() : "desconhecido";
        r.acao = logAcao.getAcao();
        r.entidade = logAcao.getEntidade();
        r.entidadeId = logAcao.getEntidadeId();
        r.timestamp = logAcao.getTimestamp();
        return r;
    }

    public Long getId() { return id; }
    public String getNomeUsuario() { return nomeUsuario; }
    public String getAcao() { return acao; }
    public String getEntidade() { return entidade; }
    public Long getEntidadeId() { return entidadeId; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

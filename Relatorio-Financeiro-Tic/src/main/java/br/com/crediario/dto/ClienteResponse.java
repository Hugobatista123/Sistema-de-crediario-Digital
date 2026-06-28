package br.com.crediario.dto;

import br.com.crediario.model.Cliente;
import br.com.crediario.model.ClientePF;
import br.com.crediario.model.ClientePJ;

import java.util.List;

public class ClienteResponse {

    private Long id;
    private String tipo;
    private String nome;
    private String sobrenome;
    private String telefone;
    private String email;
    private String contaGov;
    private String outros;
    private boolean ativo;
    private String cpf;
    private String cnpj;
    private String razaoSocial;
    private List<ServicoResponse> servicos;

    public ClienteResponse() {
    }

    public static ClienteResponse fromEntity(Cliente c) {
        ClienteResponse r = new ClienteResponse();
        r.id = c.getId();
        r.tipo = c.getTipo();
        r.nome = c.getNome();
        r.sobrenome = c.getSobrenome();
        r.telefone = c.getTelefone();
        r.email = c.getEmail();
        r.contaGov = c.getContaGov();
        r.outros = c.getOutros();
        r.ativo = c.isAtivo();

        if (c instanceof ClientePF pf) {
            r.cpf = pf.getCpf();
        } else if (c instanceof ClientePJ pj) {
            r.cnpj = pj.getCnpj();
            r.razaoSocial = pj.getRazaoSocial();
        }

        return r;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNome() {
        return nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public String getContaGov() {
        return contaGov;
    }

    public String getOutros() {
        return outros;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public String getCpf() {
        return cpf;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public List<ServicoResponse> getServicos() {
        return servicos;
    }

    public void setServicos(List<ServicoResponse> servicos) {
        this.servicos = servicos;
    }
}

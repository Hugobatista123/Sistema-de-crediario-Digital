package br.com.crediario.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PJ")
public class ClientePJ extends Cliente {

    @Column(length = 18)
    private String cnpj;

    @Column(name = "razao_social", length = 200)
    private String razaoSocial;

    protected ClientePJ() {
        super();
    }

    public ClientePJ(String nome) {
        super(nome);
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }
}

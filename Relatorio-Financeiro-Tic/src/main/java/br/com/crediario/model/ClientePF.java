package br.com.crediario.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PF")
public class ClientePF extends Cliente {

    @Column(length = 14)
    private String cpf;

    protected ClientePF() {
        super();
    }

    public ClientePF(String nome) {
        super(nome);
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}

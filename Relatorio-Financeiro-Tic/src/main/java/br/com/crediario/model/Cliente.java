package br.com.crediario.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 2)
public abstract class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo", insertable = false, updatable = false, length = 2)
    private String tipo;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 200)
    private String sobrenome;

    @Column(length = 20)
    private String telefone;

    @Column(length = 200)
    private String email;

    @Column(name = "conta_gov", length = 200)
    private String contaGov;

    @Column(columnDefinition = "TEXT")
    private String outros;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Cliente() {
    }

    public Cliente(String nome) {
        this.nome = nome;
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

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContaGov() {
        return contaGov;
    }

    public void setContaGov(String contaGov) {
        this.contaGov = contaGov;
    }

    public String getOutros() {
        return outros;
    }

    public void setOutros(String outros) {
        this.outros = outros;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}

package br.com.crediario.dto;

public class LoginResponse {
    private final String nome;
    private final String perfil;

    public LoginResponse(String nome, String perfil) {
        this.nome = nome;
        this.perfil = perfil;
    }

    public String getNome() { return nome; }
    public String getPerfil() { return perfil; }
}

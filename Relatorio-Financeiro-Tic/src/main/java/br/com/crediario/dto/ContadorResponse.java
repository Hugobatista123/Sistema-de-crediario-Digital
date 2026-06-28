package br.com.crediario.dto;

public class ContadorResponse {

    private int quantidade;

    public ContadorResponse(int quantidade) {
        this.quantidade = quantidade;
    }

    public int getQuantidade() { return quantidade; }
}

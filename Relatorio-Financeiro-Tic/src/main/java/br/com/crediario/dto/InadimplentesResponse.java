package br.com.crediario.dto;

import java.util.List;

public class InadimplentesResponse {

    private List<ClienteInadimplente> inadimplentes;

    public InadimplentesResponse(List<ClienteInadimplente> inadimplentes) {
        this.inadimplentes = inadimplentes;
    }

    public List<ClienteInadimplente> getInadimplentes() { return inadimplentes; }
}

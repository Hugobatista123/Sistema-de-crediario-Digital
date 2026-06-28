package br.com.crediario.dto;

import br.com.crediario.model.StatusCobranca;

public class ServicoStatusRequest {

    private StatusCobranca novoStatus;

    public StatusCobranca getNovoStatus() {
        return novoStatus;
    }

    public void setNovoStatus(StatusCobranca novoStatus) {
        this.novoStatus = novoStatus;
    }
}

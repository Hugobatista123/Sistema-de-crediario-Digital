package br.com.crediario.controller;

import br.com.crediario.dto.FaturamentoResponse;
import br.com.crediario.dto.HistoricoClienteResponse;
import br.com.crediario.dto.InadimplentesResponse;
import br.com.crediario.dto.TotaisStatusResponse;
import br.com.crediario.model.StatusCobranca;
import br.com.crediario.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/faturamento")
    public ResponseEntity<?> faturamento(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Os parametros 'inicio' e 'fim' sao obrigatorios");
        }
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data de inicio nao pode ser posterior a data de fim");
        }
        FaturamentoResponse response = relatorioService.faturamentoPorPeriodo(inicio, fim);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<HistoricoClienteResponse> historicoPorCliente(@PathVariable Long id) {
        return ResponseEntity.ok(relatorioService.historicoPorCliente(id));
    }

    // Retorna InadimplentesResponse (wrapper com campo "inadimplentes": [...]).
    // Diferente de GET /api/inadimplentes, que retorna array plano de ClienteInadimplente.
    // Este endpoint é mantido para integrações que precisam do objeto envelope.
    @GetMapping("/inadimplencia")
    public ResponseEntity<InadimplentesResponse> inadimplencia() {
        return ResponseEntity.ok(relatorioService.listarInadimplentes());
    }

    @GetMapping("/totais")
    public ResponseEntity<?> totais(@RequestParam(required = false) String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("O parametro 'status' e obrigatorio");
        }
        StatusCobranca statusEnum;
        try {
            statusEnum = StatusCobranca.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status invalido: " + status + ". Valores validos: A_COBRAR, PENDENTE, PAGO");
        }
        TotaisStatusResponse response = relatorioService.totaisPorStatus(statusEnum);
        return ResponseEntity.ok(response);
    }
}

package br.com.crediario.controller;

import br.com.crediario.dto.ClienteInadimplente;
import br.com.crediario.dto.ContadorResponse;
import br.com.crediario.service.RelatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inadimplentes")
public class InadimplentesController {

    private final RelatorioService relatorioService;

    public InadimplentesController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteInadimplente>> listar() {
        return ResponseEntity.ok(relatorioService.listarInadimplentes().getInadimplentes());
    }

    @GetMapping("/count")
    public ResponseEntity<ContadorResponse> count() {
        return ResponseEntity.ok(relatorioService.contarInadimplentes());
    }
}

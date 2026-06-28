package br.com.crediario.controller;

import br.com.crediario.dto.PagamentoParcialRequest;
import br.com.crediario.dto.PagamentoParcialResponse;
import br.com.crediario.service.PagamentoParcialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PagamentoParcialController {

    private final PagamentoParcialService pagamentoParcialService;

    public PagamentoParcialController(PagamentoParcialService pagamentoParcialService) {
        this.pagamentoParcialService = pagamentoParcialService;
    }

    @GetMapping("/servicos/{servicoId}/pagamentos")
    public ResponseEntity<List<PagamentoParcialResponse>> listarPorServico(@PathVariable Long servicoId) {
        return ResponseEntity.ok(pagamentoParcialService.listarPorServico(servicoId));
    }

    @PostMapping("/servicos/{servicoId}/pagamentos")
    public ResponseEntity<PagamentoParcialResponse> registrar(@PathVariable Long servicoId,
                                                              @RequestBody PagamentoParcialRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagamentoParcialService.registrar(servicoId, dto));
    }
}

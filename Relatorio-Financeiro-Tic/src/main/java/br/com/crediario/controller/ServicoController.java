package br.com.crediario.controller;

import br.com.crediario.dto.ServicoRequest;
import br.com.crediario.dto.ServicoResponse;
import br.com.crediario.dto.ServicoStatusRequest;
import br.com.crediario.service.ServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @GetMapping("/clientes/{clienteId}/servicos")
    public ResponseEntity<List<ServicoResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(servicoService.listarPorCliente(clienteId));
    }

    @PostMapping("/clientes/{clienteId}/servicos")
    public ResponseEntity<ServicoResponse> registrar(@PathVariable Long clienteId,
                                                     @RequestBody ServicoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicoService.registrar(clienteId, dto));
    }

    @GetMapping("/servicos/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @GetMapping("/servicos/excluidos")
    public ResponseEntity<List<ServicoResponse>> listarExcluidos() {
        return ResponseEntity.ok(servicoService.listarExcluidos());
    }

    @PutMapping("/servicos/{id}")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody ServicoRequest dto) {
        return ResponseEntity.ok(servicoService.atualizar(id, dto));
    }

    @DeleteMapping("/servicos/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        servicoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/servicos/{id}/status")
    public ResponseEntity<ServicoResponse> alterarStatus(@PathVariable Long id,
                                                         @RequestBody ServicoStatusRequest dto) {
        return ResponseEntity.ok(servicoService.alterarStatus(id, dto.getNovoStatus()));
    }

    @PutMapping("/servicos/{id}/restaurar")
    public ResponseEntity<ServicoResponse> restaurar(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.restaurar(id));
    }
}

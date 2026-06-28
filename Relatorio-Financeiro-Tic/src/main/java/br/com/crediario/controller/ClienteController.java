package br.com.crediario.controller;

import br.com.crediario.dto.ClienteRequest;
import br.com.crediario.dto.ClienteResponse;
import br.com.crediario.service.ClienteService;
import br.com.crediario.service.ServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ServicoService servicoService;

    public ClienteController(ClienteService clienteService, ServicoService servicoService) {
        this.clienteService = clienteService;
        this.servicoService = servicoService;
    }

    @GetMapping("/excluidos")
    public List<ClienteResponse> listarExcluidos() {
        return clienteService.listarExcluidos();
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(required = false) String nome) {
        if (nome != null && !nome.isBlank()) {
            return clienteService.buscarPorNome(nome);
        }
        return clienteService.listar();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@PathVariable Long id) {
        ClienteResponse response = clienteService.buscarPorId(id);
        response.setServicos(servicoService.listarPorCliente(id));
        return response;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> salvar(@RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.salvar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id, @RequestBody ClienteRequest request) {
        return clienteService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        clienteService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restaurar")
    public ClienteResponse restaurar(@PathVariable Long id) {
        return clienteService.restaurar(id);
    }
}

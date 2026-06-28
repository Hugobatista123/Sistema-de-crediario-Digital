package br.com.crediario.service;

import br.com.crediario.dto.ClienteRequest;
import br.com.crediario.dto.ClienteResponse;
import br.com.crediario.model.Cliente;
import br.com.crediario.model.ClientePF;
import br.com.crediario.model.ClientePJ;
import br.com.crediario.repository.ClienteRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final LogAcaoService logAcaoService;

    public ClienteService(ClienteRepository clienteRepository, LogAcaoService logAcaoService) {
        this.clienteRepository = clienteRepository;
        this.logAcaoService = logAcaoService;
    }

    // Login obtido via SecurityContextHolder — sem alterar assinaturas dos métodos públicos
    private String loginAtual() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return (auth != null && auth.isAuthenticated()) ? auth.getName() : "sistema";
        } catch (Exception e) {
            return "sistema";
        }
    }

    public List<ClienteResponse> listar() {
        return clienteRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(ClienteResponse::fromEntity)
                .toList();
    }

    public List<ClienteResponse> buscarPorNome(String termo) {
        return clienteRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(termo)
                .stream()
                .map(ClienteResponse::fromEntity)
                .toList();
    }

    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado com id: " + id));
        return ClienteResponse.fromEntity(cliente);
    }

    @Transactional
    public ClienteResponse salvar(ClienteRequest request) {
        if (request.getNome() == null || request.getNome().isBlank()) {
            throw new IllegalArgumentException("O campo nome e obrigatorio");
        }

        Cliente cliente = criarEntidade(request);
        preencherCamposComuns(cliente, request);
        preencherCamposEspecificos(cliente, request);

        ClienteResponse saved = ClienteResponse.fromEntity(clienteRepository.save(cliente));
        logAcaoService.registrar(loginAtual(), "CADASTRAR_CLIENTE", "Cliente", saved.getId());
        return saved;
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado com id: " + id));

        if (request.getNome() != null && !request.getNome().isBlank()) {
            cliente.setNome(request.getNome());
        }
        preencherCamposComuns(cliente, request);
        preencherCamposEspecificos(cliente, request);

        ClienteResponse updated = ClienteResponse.fromEntity(clienteRepository.save(cliente));
        logAcaoService.registrar(loginAtual(), "ATUALIZAR_CLIENTE", "Cliente", id);
        return updated;
    }

    @Transactional
    public void remover(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado com id: " + id));
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
        logAcaoService.registrar(loginAtual(), "REMOVER_CLIENTE", "Cliente", id);
    }

    @Transactional
    public ClienteResponse restaurar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado com id: " + id));
        cliente.setAtivo(true);
        ClienteResponse restored = ClienteResponse.fromEntity(clienteRepository.save(cliente));
        logAcaoService.registrar(loginAtual(), "RESTAURAR_CLIENTE", "Cliente", id);
        return restored;
    }

    public List<ClienteResponse> listarExcluidos() {
        return clienteRepository.findByAtivoFalseOrderByNomeAsc()
                .stream()
                .map(ClienteResponse::fromEntity)
                .toList();
    }

    private Cliente criarEntidade(ClienteRequest request) {
        if ("PJ".equalsIgnoreCase(request.getTipo())) {
            return new ClientePJ(request.getNome());
        }
        return new ClientePF(request.getNome());
    }

    private void preencherCamposComuns(Cliente cliente, ClienteRequest request) {
        if (request.getSobrenome() != null) {
            cliente.setSobrenome(request.getSobrenome());
        }
        if (request.getTelefone() != null) {
            cliente.setTelefone(request.getTelefone());
        }
        if (request.getEmail() != null) {
            cliente.setEmail(request.getEmail());
        }
        if (request.getContaGov() != null) {
            cliente.setContaGov(request.getContaGov());
        }
        if (request.getOutros() != null) {
            cliente.setOutros(request.getOutros());
        }
    }

    private void preencherCamposEspecificos(Cliente cliente, ClienteRequest request) {
        if (cliente instanceof ClientePF pf) {
            pf.setCpf(request.getCpf());
        } else if (cliente instanceof ClientePJ pj) {
            pj.setCnpj(request.getCnpj());
            pj.setRazaoSocial(request.getRazaoSocial());
        }
    }
}

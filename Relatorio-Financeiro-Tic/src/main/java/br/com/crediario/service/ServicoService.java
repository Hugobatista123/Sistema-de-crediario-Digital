package br.com.crediario.service;

import br.com.crediario.dto.ServicoRequest;
import br.com.crediario.dto.ServicoResponse;
import br.com.crediario.model.Categoria;
import br.com.crediario.model.Cliente;
import br.com.crediario.model.PagamentoParcial;
import br.com.crediario.model.Servico;
import br.com.crediario.model.StatusCobranca;
import br.com.crediario.repository.CategoriaRepository;
import br.com.crediario.repository.ClienteRepository;
import br.com.crediario.repository.PagamentoParcialRepository;
import br.com.crediario.repository.ServicoRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final PagamentoParcialRepository pagamentoParcialRepository;
    private final LogAcaoService logAcaoService;

    public ServicoService(ServicoRepository servicoRepository,
                          ClienteRepository clienteRepository,
                          CategoriaRepository categoriaRepository,
                          PagamentoParcialRepository pagamentoParcialRepository,
                          LogAcaoService logAcaoService) {
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.pagamentoParcialRepository = pagamentoParcialRepository;
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

    public List<ServicoResponse> listarPorCliente(Long clienteId) {
        return servicoRepository.findByClienteIdAndAtivoTrueOrderByDataDesc(clienteId)
                .stream()
                .map(s -> ServicoResponse.fromEntity(s, calcularSaldo(s)))
                .toList();
    }

    public ServicoResponse buscarPorId(Long id) {
        Servico servico = encontrarServico(id);
        return ServicoResponse.fromEntity(servico, calcularSaldo(servico));
    }

    @Transactional
    public ServicoResponse registrar(Long clienteId, ServicoRequest dto) {
        if (dto.getDescricao() == null || dto.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descricao e obrigatoria");
        }
        if (dto.getData() == null) {
            throw new IllegalArgumentException("Data e obrigatoria");
        }
        if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor deve ser nao negativo");
        }
        if (dto.getCategoriaId() == null) {
            throw new IllegalArgumentException("Categoria e obrigatoria");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado: " + clienteId));
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new NoSuchElementException("Categoria nao encontrada: " + dto.getCategoriaId()));

        Servico servico = new Servico();
        servico.setDescricao(dto.getDescricao());
        servico.setData(dto.getData());
        servico.setValor(dto.getValor());
        servico.setCliente(cliente);
        servico.setCategoria(categoria);

        Servico saved = servicoRepository.save(servico);
        logAcaoService.registrar(loginAtual(), "CADASTRAR_SERVICO", "Servico", saved.getId());
        return ServicoResponse.fromEntity(saved, BigDecimal.ZERO);
    }

    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest dto) {
        Servico servico = encontrarServico(id);

        if (dto.getDescricao() != null && !dto.getDescricao().isBlank()) {
            servico.setDescricao(dto.getDescricao());
        }
        if (dto.getData() != null) {
            servico.setData(dto.getData());
        }
        if (dto.getValor() != null) {
            if (dto.getValor().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Valor deve ser nao negativo");
            }
            servico.setValor(dto.getValor());
        }

        Servico saved = servicoRepository.save(servico);
        logAcaoService.registrar(loginAtual(), "ATUALIZAR_SERVICO", "Servico", id);
        return ServicoResponse.fromEntity(saved, calcularSaldo(saved));
    }

    @Transactional
    public ServicoResponse alterarStatus(Long id, StatusCobranca novoStatus) {
        Servico servico = encontrarServico(id);
        StatusCobranca atual = servico.getStatus();

        boolean transicaoValida = (atual == StatusCobranca.A_COBRAR && novoStatus == StatusCobranca.PENDENTE)
                || (atual == StatusCobranca.PENDENTE && novoStatus == StatusCobranca.PAGO);

        if (!transicaoValida) {
            throw new IllegalStateException(
                    "Transicao invalida de status: " + atual + " -> " + novoStatus);
        }

        servico.setStatus(novoStatus);
        Servico saved = servicoRepository.save(servico);
        logAcaoService.registrar(loginAtual(), "ALTERAR_STATUS_SERVICO", "Servico", id);
        return ServicoResponse.fromEntity(saved, calcularSaldo(saved));
    }

    @Transactional
    public void remover(Long id) {
        Servico servico = encontrarServico(id);
        servico.setAtivo(false);
        servicoRepository.save(servico);
        logAcaoService.registrar(loginAtual(), "REMOVER_SERVICO", "Servico", id);
    }

    @Transactional
    public ServicoResponse restaurar(Long id) {
        Servico servico = encontrarServico(id);
        servico.setAtivo(true);
        Servico saved = servicoRepository.save(servico);
        logAcaoService.registrar(loginAtual(), "RESTAURAR_SERVICO", "Servico", id);
        return ServicoResponse.fromEntity(saved, calcularSaldo(saved));
    }

    public List<ServicoResponse> listarExcluidos() {
        return servicoRepository.findByAtivoFalse()
                .stream()
                .map(s -> ServicoResponse.fromEntity(s, calcularSaldo(s)))
                .toList();
    }

    public BigDecimal calcularSaldoDevedor(Long servicoId) {
        Servico servico = encontrarServico(servicoId);
        return calcularSaldo(servico);
    }

    Servico encontrarServico(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Servico nao encontrado: " + id));
    }

    BigDecimal calcularSaldo(Servico servico) {
        List<PagamentoParcial> pagamentos = pagamentoParcialRepository.findByServicoId(servico.getId());
        BigDecimal totalPago = pagamentos.stream()
                .map(PagamentoParcial::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = servico.getValor().subtract(totalPago);
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }
}

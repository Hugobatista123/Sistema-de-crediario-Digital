package br.com.crediario.service;

import br.com.crediario.dto.PagamentoParcialRequest;
import br.com.crediario.dto.PagamentoParcialResponse;
import br.com.crediario.model.PagamentoParcial;
import br.com.crediario.model.Servico;
import br.com.crediario.model.StatusCobranca;
import br.com.crediario.repository.PagamentoParcialRepository;
import br.com.crediario.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PagamentoParcialService {

    private final PagamentoParcialRepository pagamentoParcialRepository;
    private final ServicoRepository servicoRepository;
    private final ServicoService servicoService;

    public PagamentoParcialService(PagamentoParcialRepository pagamentoParcialRepository,
                                   ServicoRepository servicoRepository,
                                   ServicoService servicoService) {
        this.pagamentoParcialRepository = pagamentoParcialRepository;
        this.servicoRepository = servicoRepository;
        this.servicoService = servicoService;
    }

    public List<PagamentoParcialResponse> listarPorServico(Long servicoId) {
        return pagamentoParcialRepository.findByServicoId(servicoId)
                .stream()
                .map(PagamentoParcialResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PagamentoParcialResponse registrar(Long servicoId, PagamentoParcialRequest dto) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new NoSuchElementException("Servico nao encontrado: " + servicoId));

        if (servico.getStatus() != StatusCobranca.PENDENTE) {
            throw new IllegalStateException(
                    "Servico deve estar com status PENDENTE para receber pagamento");
        }

        BigDecimal saldoAtual = servicoService.calcularSaldo(servico);
        BigDecimal novoSaldo = saldoAtual.subtract(dto.getValorPago());

        if (novoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Pagamento excede o saldo devedor");
        }

        PagamentoParcial pagamento = new PagamentoParcial();
        pagamento.setServico(servico);
        pagamento.setValorPago(dto.getValorPago());
        pagamento.setDataPagamento(dto.getDataPagamento());
        PagamentoParcial saved = pagamentoParcialRepository.save(pagamento);

        if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            servicoService.alterarStatus(servicoId, StatusCobranca.PAGO);
        }

        return PagamentoParcialResponse.fromEntity(saved);
    }
}

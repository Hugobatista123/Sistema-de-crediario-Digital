package br.com.crediario.service;

import br.com.crediario.dto.ClienteInadimplente;
import br.com.crediario.dto.ClienteResponse;
import br.com.crediario.dto.ContadorResponse;
import br.com.crediario.dto.FaturamentoResponse;
import br.com.crediario.dto.HistoricoClienteResponse;
import br.com.crediario.dto.InadimplentesResponse;
import br.com.crediario.dto.ItemFaturamento;
import br.com.crediario.dto.PagamentoParcialResponse;
import br.com.crediario.dto.ServicoComPagamentos;
import br.com.crediario.dto.TotaisStatusResponse;
import br.com.crediario.model.Cliente;
import br.com.crediario.model.PagamentoParcial;
import br.com.crediario.model.Servico;
import br.com.crediario.model.StatusCobranca;
import br.com.crediario.repository.ClienteRepository;
import br.com.crediario.repository.PagamentoParcialRepository;
import br.com.crediario.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class RelatorioService {

    private final ServicoRepository servicoRepository;
    private final PagamentoParcialRepository pagamentoParcialRepository;
    private final ClienteRepository clienteRepository;

    public RelatorioService(ServicoRepository servicoRepository,
                            PagamentoParcialRepository pagamentoParcialRepository,
                            ClienteRepository clienteRepository) {
        this.servicoRepository = servicoRepository;
        this.pagamentoParcialRepository = pagamentoParcialRepository;
        this.clienteRepository = clienteRepository;
    }

    public FaturamentoResponse faturamentoPorPeriodo(LocalDate inicio, LocalDate fim) {
        List<Servico> pagos = servicoRepository.findByStatusAndDataBetween(StatusCobranca.PAGO, inicio, fim);
        List<ItemFaturamento> itens = pagos.stream()
                .map(s -> new ItemFaturamento(
                        s.getCliente().getNome(),
                        s.getDescricao(),
                        s.getData(),
                        s.getValor()))
                .toList();
        BigDecimal totalGeral = itens.stream()
                .map(ItemFaturamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FaturamentoResponse(itens, totalGeral);
    }

    public HistoricoClienteResponse historicoPorCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado: " + clienteId));
        List<Servico> servicos = servicoRepository.findByClienteIdOrderByDataDesc(clienteId);
        List<ServicoComPagamentos> servicosComPagamentos = servicos.stream()
                .map(s -> {
                    List<PagamentoParcial> pagamentos = pagamentoParcialRepository.findByServicoId(s.getId());
                    BigDecimal saldo = calcularSaldo(s, pagamentos);
                    List<PagamentoParcialResponse> resps = pagamentos.stream()
                            .map(PagamentoParcialResponse::fromEntity)
                            .toList();
                    return new ServicoComPagamentos(s, resps, saldo);
                })
                .toList();
        return new HistoricoClienteResponse(ClienteResponse.fromEntity(cliente), servicosComPagamentos);
    }

    public InadimplentesResponse listarInadimplentes() {
        List<Servico> pendentes = servicoRepository.findByStatusAndAtivoTrue(StatusCobranca.PENDENTE);
        Map<Cliente, BigDecimal> totalPorCliente = new HashMap<>();
        for (Servico s : pendentes) {
            List<PagamentoParcial> pagamentos = pagamentoParcialRepository.findByServicoId(s.getId());
            BigDecimal saldo = calcularSaldo(s, pagamentos);
            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                totalPorCliente.merge(s.getCliente(), saldo, BigDecimal::add);
            }
        }
        List<ClienteInadimplente> inadimplentes = totalPorCliente.entrySet().stream()
                .map(e -> new ClienteInadimplente(
                        e.getKey().getId(),
                        e.getKey().getNome(),
                        e.getKey().getTipo(),
                        e.getValue()))
                .sorted(Comparator.comparing(ClienteInadimplente::getTotalDevido).reversed())
                .toList();
        return new InadimplentesResponse(inadimplentes);
    }

    public TotaisStatusResponse totaisPorStatus(StatusCobranca status) {
        List<Servico> servicos = servicoRepository.findByStatusAndAtivoTrue(status);
        BigDecimal total = servicos.stream()
                .map(Servico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TotaisStatusResponse(total, servicos.size());
    }

    public ContadorResponse contarInadimplentes() {
        return new ContadorResponse(listarInadimplentes().getInadimplentes().size());
    }

    private BigDecimal calcularSaldo(Servico servico, List<PagamentoParcial> pagamentos) {
        BigDecimal totalPago = pagamentos.stream()
                .map(PagamentoParcial::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = servico.getValor().subtract(totalPago);
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }
}

package br.com.crediario.service;

import br.com.crediario.dto.FaturamentoResponse;
import br.com.crediario.dto.InadimplentesResponse;
import br.com.crediario.dto.TotaisStatusResponse;
import br.com.crediario.model.Categoria;
import br.com.crediario.model.ClientePF;
import br.com.crediario.model.PagamentoParcial;
import br.com.crediario.model.Servico;
import br.com.crediario.model.StatusCobranca;
import br.com.crediario.repository.ClienteRepository;
import br.com.crediario.repository.PagamentoParcialRepository;
import br.com.crediario.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private PagamentoParcialRepository pagamentoParcialRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    private ClientePF cliente;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        cliente = new ClientePF("Ana");
        categoria = new Categoria("Consultoria");
    }

    private Servico criarServico(StatusCobranca status, BigDecimal valor) {
        Servico s = new Servico();
        s.setDescricao("Servico teste");
        s.setData(LocalDate.of(2025, 4, 15));
        s.setValor(valor);
        s.setStatus(status);
        s.setAtivo(true);
        s.setCliente(cliente);
        s.setCategoria(categoria);
        return s;
    }

    @Test
    void faturamentoPorPeriodo_retornaApenas_ServicosPAGO_noIntervalo() {
        Servico pago = criarServico(StatusCobranca.PAGO, new BigDecimal("500.00"));
        LocalDate inicio = LocalDate.of(2025, 4, 1);
        LocalDate fim = LocalDate.of(2025, 4, 30);

        when(servicoRepository.findByStatusAndDataBetween(eq(StatusCobranca.PAGO), eq(inicio), eq(fim)))
                .thenReturn(List.of(pago));

        FaturamentoResponse response = relatorioService.faturamentoPorPeriodo(inicio, fim);

        assertEquals(1, response.getItens().size());
        assertEquals(new BigDecimal("500.00"), response.getTotalGeral());
        assertEquals("Ana", response.getItens().get(0).getNomeCliente());
    }

    @Test
    void faturamentoPorPeriodo_naoRetorna_ServicosPENDENTE_ouACOBRAR() {
        LocalDate inicio = LocalDate.of(2025, 4, 1);
        LocalDate fim = LocalDate.of(2025, 4, 30);

        when(servicoRepository.findByStatusAndDataBetween(eq(StatusCobranca.PAGO), eq(inicio), eq(fim)))
                .thenReturn(Collections.emptyList());

        FaturamentoResponse response = relatorioService.faturamentoPorPeriodo(inicio, fim);

        assertTrue(response.getItens().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getTotalGeral());
    }

    @Test
    void faturamentoPorPeriodo_periodoSemResultados_retornaListaVazia() {
        LocalDate inicio = LocalDate.of(2020, 1, 1);
        LocalDate fim = LocalDate.of(2020, 1, 31);

        when(servicoRepository.findByStatusAndDataBetween(any(), eq(inicio), eq(fim)))
                .thenReturn(Collections.emptyList());

        FaturamentoResponse response = relatorioService.faturamentoPorPeriodo(inicio, fim);

        assertTrue(response.getItens().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getTotalGeral());
    }

    @Test
    void listarInadimplentes_naoRetorna_ClienteComSaldoZero() {
        Servico servicoPago = criarServico(StatusCobranca.PENDENTE, new BigDecimal("300.00"));
        PagamentoParcial pagamento = new PagamentoParcial();
        pagamento.setValorPago(new BigDecimal("300.00"));
        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setServico(servicoPago);

        when(servicoRepository.findByStatusAndAtivoTrue(StatusCobranca.PENDENTE))
                .thenReturn(List.of(servicoPago));
        when(pagamentoParcialRepository.findByServicoId(any()))
                .thenReturn(List.of(pagamento));

        InadimplentesResponse response = relatorioService.listarInadimplentes();

        assertTrue(response.getInadimplentes().isEmpty());
    }

    @Test
    void listarInadimplentes_naoRetorna_ClienteSoComServicosACOBRAR() {
        when(servicoRepository.findByStatusAndAtivoTrue(StatusCobranca.PENDENTE))
                .thenReturn(Collections.emptyList());

        InadimplentesResponse response = relatorioService.listarInadimplentes();

        assertTrue(response.getInadimplentes().isEmpty());
    }

    @Test
    void totaisPorStatus_calculaSomaCorretamente() {
        Servico s1 = criarServico(StatusCobranca.A_COBRAR, new BigDecimal("200.00"));
        Servico s2 = criarServico(StatusCobranca.A_COBRAR, new BigDecimal("350.00"));

        when(servicoRepository.findByStatusAndAtivoTrue(StatusCobranca.A_COBRAR))
                .thenReturn(List.of(s1, s2));

        TotaisStatusResponse response = relatorioService.totaisPorStatus(StatusCobranca.A_COBRAR);

        assertEquals(new BigDecimal("550.00"), response.getTotal());
        assertEquals(2, response.getQuantidade());
    }
}

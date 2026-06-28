package br.com.crediario.service;

import br.com.crediario.dto.PagamentoParcialRequest;
import br.com.crediario.dto.PagamentoParcialResponse;
import br.com.crediario.model.Categoria;
import br.com.crediario.model.ClientePF;
import br.com.crediario.model.PagamentoParcial;
import br.com.crediario.model.Servico;
import br.com.crediario.model.StatusCobranca;
import br.com.crediario.repository.PagamentoParcialRepository;
import br.com.crediario.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoParcialServiceTest {

    @Mock
    private PagamentoParcialRepository pagamentoParcialRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ServicoService servicoService;

    private PagamentoParcialService pagamentoParcialService;

    private Servico servico;

    @BeforeEach
    void setUp() {
        pagamentoParcialService = new PagamentoParcialService(
                pagamentoParcialRepository, servicoRepository, servicoService);

        servico = new Servico();
        servico.setDescricao("Servico teste");
        servico.setData(LocalDate.now());
        servico.setValor(new BigDecimal("500.00"));
        servico.setStatus(StatusCobranca.PENDENTE);
        servico.setCliente(new ClientePF("Maria"));
        servico.setCategoria(new Categoria("Financeiro"));
    }

    @Test
    void registrar_ComSaldoRestante_DeveManterStatusPendente() {
        PagamentoParcialRequest dto = new PagamentoParcialRequest();
        dto.setValorPago(new BigDecimal("200.00"));
        dto.setDataPagamento(LocalDate.now());

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoService.calcularSaldo(servico)).thenReturn(new BigDecimal("500.00"));
        when(pagamentoParcialRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PagamentoParcialResponse response = pagamentoParcialService.registrar(1L, dto);

        assertEquals(new BigDecimal("200.00"), response.getValorPago());
        verify(servicoService, never()).alterarStatus(anyLong(), any());
    }

    @Test
    void registrar_QuandoZeraSaldo_DeveAlterarStatusParaPago() {
        PagamentoParcialRequest dto = new PagamentoParcialRequest();
        dto.setValorPago(new BigDecimal("500.00"));
        dto.setDataPagamento(LocalDate.now());

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoService.calcularSaldo(servico)).thenReturn(new BigDecimal("500.00"));
        when(pagamentoParcialRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pagamentoParcialService.registrar(1L, dto);

        verify(servicoService).alterarStatus(1L, StatusCobranca.PAGO);
    }

    @Test
    void registrar_ComValorMaiorQueSaldo_DeveLancarExcecao() {
        PagamentoParcialRequest dto = new PagamentoParcialRequest();
        dto.setValorPago(new BigDecimal("600.00"));
        dto.setDataPagamento(LocalDate.now());

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoService.calcularSaldo(servico)).thenReturn(new BigDecimal("500.00"));

        assertThrows(IllegalStateException.class,
                () -> pagamentoParcialService.registrar(1L, dto));
        verify(pagamentoParcialRepository, never()).save(any());
    }

    @Test
    void registrar_EmServico_ComStatusACobrar_DeveLancarExcecao() {
        servico.setStatus(StatusCobranca.A_COBRAR);
        PagamentoParcialRequest dto = new PagamentoParcialRequest();
        dto.setValorPago(new BigDecimal("100.00"));
        dto.setDataPagamento(LocalDate.now());

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(IllegalStateException.class,
                () -> pagamentoParcialService.registrar(1L, dto));
        verify(pagamentoParcialRepository, never()).save(any());
    }
}

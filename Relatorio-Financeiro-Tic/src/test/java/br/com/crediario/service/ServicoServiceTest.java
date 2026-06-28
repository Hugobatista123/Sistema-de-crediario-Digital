package br.com.crediario.service;

import br.com.crediario.dto.ServicoRequest;
import br.com.crediario.dto.ServicoResponse;
import br.com.crediario.model.Categoria;
import br.com.crediario.model.Cliente;
import br.com.crediario.model.ClientePF;
import br.com.crediario.model.Servico;
import br.com.crediario.model.StatusCobranca;
import br.com.crediario.repository.CategoriaRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private PagamentoParcialRepository pagamentoParcialRepository;

    @Mock
    private LogAcaoService logAcaoService;

    @InjectMocks
    private ServicoService servicoService;

    private Cliente cliente;
    private Categoria categoria;
    private Servico servico;

    @BeforeEach
    void setUp() {
        cliente = new ClientePF("João");
        categoria = new Categoria("Consultoria");
        servico = new Servico();
        servico.setDescricao("Servico de teste");
        servico.setData(LocalDate.now());
        servico.setValor(new BigDecimal("500.00"));
        servico.setCliente(cliente);
        servico.setCategoria(categoria);
    }

    @Test
    void registrar_deveCriarServico_ComStatusACobrar() {
        ServicoRequest dto = new ServicoRequest();
        dto.setDescricao("Consultoria mensal");
        dto.setData(LocalDate.now());
        dto.setValor(new BigDecimal("300.00"));
        dto.setCategoriaId(1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> inv.getArgument(0));

        ServicoResponse response = servicoService.registrar(1L, dto);

        assertEquals(StatusCobranca.A_COBRAR, response.getStatus());
        assertEquals(new BigDecimal("300.00"), response.getValor());
    }

    @Test
    void alterarStatus_ACobrante_ParaPendente_DeveFuncionar() {
        servico.setStatus(StatusCobranca.A_COBRAR);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagamentoParcialRepository.findByServicoId(any())).thenReturn(Collections.emptyList());

        ServicoResponse response = servicoService.alterarStatus(1L, StatusCobranca.PENDENTE);

        assertEquals(StatusCobranca.PENDENTE, response.getStatus());
    }

    @Test
    void alterarStatus_ACobrar_ParaPago_Direto_DeveLancarExcecao() {
        servico.setStatus(StatusCobranca.A_COBRAR);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(IllegalStateException.class,
                () -> servicoService.alterarStatus(1L, StatusCobranca.PAGO));
    }

    @Test
    void alterarStatus_Pendente_ParaACobrar_Retroceder_DeveLancarExcecao() {
        servico.setStatus(StatusCobranca.PENDENTE);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(IllegalStateException.class,
                () -> servicoService.alterarStatus(1L, StatusCobranca.A_COBRAR));
    }

    @Test
    void remover_DeveSetarAtivoFalse_SemApagarDoBanco() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> inv.getArgument(0));

        servicoService.remover(1L);

        assertFalse(servico.isAtivo());
        verify(servicoRepository).save(servico);
    }
}

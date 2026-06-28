package br.com.crediario.service;

import br.com.crediario.dto.ClienteRequest;
import br.com.crediario.dto.ClienteResponse;
import br.com.crediario.model.ClientePF;
import br.com.crediario.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private LogAcaoService logAcaoService;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequest requestPF;

    @BeforeEach
    void setUp() {
        requestPF = new ClienteRequest();
        requestPF.setTipo("PF");
        requestPF.setNome("Joao");
        requestPF.setSobrenome("Silva");
        requestPF.setCpf("123.456.789-00");
    }

    @Test
    void salvarComNomeValido_retornaClienteResponse() {
        ClientePF pf = new ClientePF("Joao");
        when(clienteRepository.save(any())).thenReturn(pf);

        ClienteResponse response = clienteService.salvar(requestPF);

        assertNotNull(response);
        verify(clienteRepository, times(1)).save(any());
    }

    @Test
    void salvarComNomeNulo_lancaIllegalArgumentException() {
        requestPF.setNome(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.salvar(requestPF)
        );

        assertNotNull(ex.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void salvarComNomeVazio_lancaIllegalArgumentException() {
        requestPF.setNome("   ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.salvar(requestPF)
        );

        assertNotNull(ex.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void remover_setaAtivoFalse() {
        ClientePF pf = new ClientePF("Joao");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(pf));
        when(clienteRepository.save(any())).thenReturn(pf);

        clienteService.remover(1L);

        ArgumentCaptor<ClientePF> captor = ArgumentCaptor.forClass(ClientePF.class);
        verify(clienteRepository).save(captor.capture());
        assertFalse(captor.getValue().isAtivo());
    }

    @Test
    void restaurar_setaAtivoTrue() {
        ClientePF pf = new ClientePF("Joao");
        pf.setAtivo(false);

        when(clienteRepository.findById(2L)).thenReturn(Optional.of(pf));
        when(clienteRepository.save(any())).thenReturn(pf);

        ClienteResponse response = clienteService.restaurar(2L);

        ArgumentCaptor<ClientePF> captor = ArgumentCaptor.forClass(ClientePF.class);
        verify(clienteRepository).save(captor.capture());
        assertTrue(captor.getValue().isAtivo());
        assertNotNull(response);
    }
}

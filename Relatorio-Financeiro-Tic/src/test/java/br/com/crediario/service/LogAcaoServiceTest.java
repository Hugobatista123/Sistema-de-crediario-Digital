package br.com.crediario.service;

import br.com.crediario.model.LogAcao;
import br.com.crediario.model.PerfilUsuario;
import br.com.crediario.model.Usuario;
import br.com.crediario.repository.LogAcaoRepository;
import br.com.crediario.repository.UsuarioRepository;
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
class LogAcaoServiceTest {

    @Mock
    private LogAcaoRepository logAcaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private LogAcaoService logAcaoService;

    private Usuario criarUsuario() {
        Usuario u = new Usuario();
        u.setNome("Admin");
        u.setLogin("admin");
        u.setSenha("hash");
        u.setPerfil(PerfilUsuario.ADMIN);
        return u;
    }

    @Test
    void registrar_usuarioExistente_persisteLogComCamposCorretos() {
        Usuario usuario = criarUsuario();
        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(usuario));

        logAcaoService.registrar("admin", "CADASTRAR_CLIENTE", "Cliente", 1L);

        ArgumentCaptor<LogAcao> captor = ArgumentCaptor.forClass(LogAcao.class);
        verify(logAcaoRepository).save(captor.capture());
        LogAcao log = captor.getValue();
        assertEquals("CADASTRAR_CLIENTE", log.getAcao());
        assertEquals("Cliente", log.getEntidade());
        assertEquals(1L, log.getEntidadeId());
        assertNotNull(log.getTimestamp());
        assertEquals(usuario, log.getUsuario());
    }

    @Test
    void registrar_usuarioInexistente_naoLancaExcecaoENaoPersiste() {
        when(usuarioRepository.findByLogin("inexistente")).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
            logAcaoService.registrar("inexistente", "ACAO", "Entidade", null));

        verify(logAcaoRepository, never()).save(any());
    }

    @Test
    void registrar_erroAoPersistir_naoLancaExcecaoParaOperacaoPrincipal() {
        Usuario usuario = criarUsuario();
        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(usuario));
        when(logAcaoRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() ->
            logAcaoService.registrar("admin", "ACAO", "Entidade", null));
    }

    @Test
    void registrar_entidadeIdNulo_persisteLogSemProblemas() {
        Usuario usuario = criarUsuario();
        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(usuario));

        logAcaoService.registrar("admin", "REMOVER_SERVICO", "Servico", null);

        ArgumentCaptor<LogAcao> captor = ArgumentCaptor.forClass(LogAcao.class);
        verify(logAcaoRepository).save(captor.capture());
        assertNull(captor.getValue().getEntidadeId());
    }
}

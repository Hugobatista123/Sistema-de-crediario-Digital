package br.com.crediario.service;

import br.com.crediario.model.PerfilUsuario;
import br.com.crediario.model.Usuario;
import br.com.crediario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario criarUsuario(String login, PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setNome("Teste");
        u.setLogin(login);
        u.setSenha("hash");
        u.setPerfil(perfil);
        return u;
    }

    @Test
    void loadUserByUsername_usuarioExistente_retornaUserDetails() {
        Usuario usuario = criarUsuario("admin", PerfilUsuario.ADMIN);
        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(usuario));

        var result = usuarioService.loadUserByUsername("admin");

        assertEquals("admin", result.getUsername());
    }

    @Test
    void loadUserByUsername_usuarioInexistente_lancaUsernameNotFoundException() {
        when(usuarioRepository.findByLogin("inexistente")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> usuarioService.loadUserByUsername("inexistente"));
    }

    @Test
    void buscarPorLogin_usuarioExistente_retornaOptionalPresente() {
        Usuario usuario = criarUsuario("admin", PerfilUsuario.ADMIN);
        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(usuario));

        var result = usuarioService.buscarPorLogin("admin");

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getLogin());
    }

    @Test
    void buscarPorLogin_usuarioInexistente_retornaOptionalVazio() {
        when(usuarioRepository.findByLogin("nenhum")).thenReturn(Optional.empty());

        var result = usuarioService.buscarPorLogin("nenhum");

        assertFalse(result.isPresent());
    }

    @Test
    void getUsuarioLogado_autenticacaoNula_lancaIllegalStateException() {
        assertThrows(IllegalStateException.class,
            () -> usuarioService.getUsuarioLogado(null));
    }

    @Test
    void getUsuarioLogado_autenticacaoValida_retornaUsuario() {
        Usuario usuario = criarUsuario("admin", PerfilUsuario.ADMIN);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("admin");
        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.getUsuarioLogado(auth);

        assertEquals("admin", result.getLogin());
    }
}

package br.com.crediario.controller;

import br.com.crediario.dto.LoginRequest;
import br.com.crediario.dto.LoginResponse;
import br.com.crediario.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getSenha())
            );

            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authentication);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

            Usuario usuario = (Usuario) authentication.getPrincipal();
            return ResponseEntity.ok(new LoginResponse(usuario.getNome(), usuario.getPerfil().name()));

        } catch (BadCredentialsException | org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // Mensagem genérica — evita enumeração de usuários
            return ResponseEntity.status(401).body(Map.of("erro", "Login ou senha inválidos"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("erro", "Login ou senha inválidos"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("mensagem", "Sessão encerrada"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(Map.of(
            "nome", usuario.getNome(),
            "login", usuario.getLogin(),
            "perfil", usuario.getPerfil().name()
        ));
    }
}

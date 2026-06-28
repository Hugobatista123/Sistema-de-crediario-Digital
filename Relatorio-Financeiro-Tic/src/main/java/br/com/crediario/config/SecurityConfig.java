package br.com.crediario.config;

import br.com.crediario.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioService usuarioService;

    public SecurityConfig(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // Mantém configuração CORS do CorsConfig existente
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                .requestMatchers("/css/**", "/js/**", "/*.html", "/favicon.ico").permitAll()
                // Apenas ADMIN
                .requestMatchers("/api/relatorios/**").hasRole("ADMIN")
                .requestMatchers("/api/logs/**").hasRole("ADMIN")
                // FUNCIONARIO e ADMIN
                .requestMatchers("/api/clientes/**").hasAnyRole("FUNCIONARIO", "ADMIN")
                .requestMatchers("/api/servicos/**").hasAnyRole("FUNCIONARIO", "ADMIN")
                .requestMatchers("/api/inadimplentes/**").hasAnyRole("FUNCIONARIO", "ADMIN")
                .requestMatchers("/api/categorias/**").hasAnyRole("FUNCIONARIO", "ADMIN")
                // Demais endpoints autenticados (ex: /api/auth/me)
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                // 401 — não redirecionar, retornar JSON
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"erro\":\"Não autenticado\"}");
                })
                // 403 — retornar JSON
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"erro\":\"Acesso negado\"}");
                })
            )
            .sessionManagement(session -> session
                .maximumSessions(15)
            )
            .userDetailsService(usuarioService);

        return http.build();
    }
}

package br.com.crediario.service;

import br.com.crediario.model.LogAcao;
import br.com.crediario.model.Usuario;
import br.com.crediario.repository.LogAcaoRepository;
import br.com.crediario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LogAcaoService {

    private static final Logger log = LoggerFactory.getLogger(LogAcaoService.class);

    private final LogAcaoRepository logAcaoRepository;
    private final UsuarioRepository usuarioRepository;

    public LogAcaoService(LogAcaoRepository logAcaoRepository, UsuarioRepository usuarioRepository) {
        this.logAcaoRepository = logAcaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrar(String login, String acao, String entidade, Long entidadeId) {
        try {
            Usuario usuario = usuarioRepository.findByLogin(login).orElse(null);
            if (usuario == null) {
                log.warn("LogAcaoService: usuário '{}' não encontrado ao registrar ação '{}'", login, acao);
                return;
            }
            LogAcao logAcao = new LogAcao();
            logAcao.setUsuario(usuario);
            logAcao.setAcao(acao);
            logAcao.setEntidade(entidade);
            logAcao.setEntidadeId(entidadeId);
            logAcao.setTimestamp(LocalDateTime.now());
            logAcaoRepository.save(logAcao);
        } catch (Exception e) {
            // Log não pode interromper a operação principal
            log.error("Falha ao registrar log de ação '{}' para usuário '{}': {}", acao, login, e.getMessage());
        }
    }
}

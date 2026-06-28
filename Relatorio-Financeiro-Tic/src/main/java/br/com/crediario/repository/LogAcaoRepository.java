package br.com.crediario.repository;

import br.com.crediario.model.LogAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogAcaoRepository extends JpaRepository<LogAcao, Long> {
    List<LogAcao> findAllByOrderByTimestampDesc();
    List<LogAcao> findByUsuarioIdOrderByTimestampDesc(Long usuarioId);
}

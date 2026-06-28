package br.com.crediario.repository;

import br.com.crediario.model.PagamentoParcial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoParcialRepository extends JpaRepository<PagamentoParcial, Long> {

    List<PagamentoParcial> findByServicoId(Long servicoId);
}

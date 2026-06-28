package br.com.crediario.repository;

import br.com.crediario.model.Servico;
import br.com.crediario.model.StatusCobranca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findByClienteIdAndAtivoTrue(Long clienteId);

    List<Servico> findByClienteIdAndAtivoTrueOrderByDataDesc(Long clienteId);

    List<Servico> findByClienteIdOrderByDataDesc(Long clienteId);

    List<Servico> findByAtivoFalse();

    List<Servico> findByStatus(StatusCobranca status);

    List<Servico> findByStatusAndAtivoTrue(StatusCobranca status);

    List<Servico> findByStatusAndDataBetween(StatusCobranca status, LocalDate inicio, LocalDate fim);
}

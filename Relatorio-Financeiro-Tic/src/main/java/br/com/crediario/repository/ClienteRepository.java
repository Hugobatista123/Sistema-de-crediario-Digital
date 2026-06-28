package br.com.crediario.repository;

import br.com.crediario.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);

    List<Cliente> findByAtivoTrueOrderByNomeAsc();

    List<Cliente> findByAtivoFalseOrderByNomeAsc();
}

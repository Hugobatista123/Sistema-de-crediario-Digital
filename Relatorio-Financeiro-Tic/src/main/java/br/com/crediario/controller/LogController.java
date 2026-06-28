package br.com.crediario.controller;

import br.com.crediario.dto.LogAcaoResponse;
import br.com.crediario.repository.LogAcaoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogAcaoRepository logAcaoRepository;

    public LogController(LogAcaoRepository logAcaoRepository) {
        this.logAcaoRepository = logAcaoRepository;
    }

    @GetMapping
    public List<LogAcaoResponse> listar() {
        return logAcaoRepository.findAllByOrderByTimestampDesc()
            .stream()
            .map(LogAcaoResponse::from)
            .collect(Collectors.toList());
    }
}

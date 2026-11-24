package br.com.unisales.horarios.controller;

import br.com.unisales.horarios.dto.GradeTurmaResponse;
import br.com.unisales.horarios.dto.TurmaRequest;
import br.com.unisales.horarios.dto.TurmaResponse;
import br.com.unisales.horarios.service.TurmaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/turmas")
@Tag(name = "Turmas", description = "CRUD de turmas e consulta da grade de horarios")
@SecurityRequirement(name = "basicAuth")
@ApiResponses(@ApiResponse(responseCode = "401", description = "Credenciais ausentes ou invalidas"))
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as turmas", description = "Retorna as turmas em ordem de identificacao.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<TurmaResponse>> listar() {
        return ResponseEntity.ok(turmaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma turma por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma encontrada"),
            @ApiResponse(responseCode = "404", description = "Turma nao encontrada")
    })
    public ResponseEntity<TurmaResponse> buscarPorId(
            @Parameter(description = "ID da turma", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(turmaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma turma")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turma criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "409", description = "Identificacao ja cadastrada")
    })
    public ResponseEntity<TurmaResponse> criar(@Valid @RequestBody TurmaRequest request) {
        TurmaResponse criada = turmaService.criar(request);
        return ResponseEntity.created(URI.create("/api/turmas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma turma")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Turma nao encontrada"),
            @ApiResponse(responseCode = "409", description = "Identificacao ja utilizada por outra turma")
    })
    public ResponseEntity<TurmaResponse> atualizar(
            @Parameter(description = "ID da turma", example = "1") @PathVariable Long id,
            @Valid @RequestBody TurmaRequest request) {
        return ResponseEntity.ok(turmaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma turma",
            description = "A exclusao e bloqueada quando a turma possui horarios vinculados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Turma excluida"),
            @ApiResponse(responseCode = "404", description = "Turma nao encontrada"),
            @ApiResponse(responseCode = "409", description = "Turma possui horarios vinculados")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da turma", example = "1") @PathVariable Long id) {
        turmaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/horarios")
    @Operation(summary = "Consulta a grade de horarios da turma",
            description = "Retorna as aulas da turma com professor, disciplina, dia da semana e horarios, "
                    + "ordenadas por dia e hora de inicio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grade retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Turma nao encontrada")
    })
    public ResponseEntity<GradeTurmaResponse> buscarGrade(
            @Parameter(description = "ID da turma", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(turmaService.buscarGrade(id));
    }
}

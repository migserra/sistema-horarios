package br.com.unisales.horarios.controller;

import br.com.unisales.horarios.dto.GradeProfessorResponse;
import br.com.unisales.horarios.dto.ProfessorRequest;
import br.com.unisales.horarios.dto.ProfessorResponse;
import br.com.unisales.horarios.service.ProfessorService;
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
@RequestMapping("/api/professores")
@Tag(name = "Professores", description = "CRUD de professores e consulta da grade de horarios")
@SecurityRequirement(name = "basicAuth")
@ApiResponses(@ApiResponse(responseCode = "401", description = "Credenciais ausentes ou invalidas"))
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os professores", description = "Retorna os professores cadastrados em ordem alfabetica.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<ProfessorResponse>> listar() {
        return ResponseEntity.ok(professorService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um professor por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor encontrado"),
            @ApiResponse(responseCode = "404", description = "Professor nao encontrado")
    })
    public ResponseEntity<ProfessorResponse> buscarPorId(
            @Parameter(description = "ID do professor", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(professorService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um professor")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Professor criado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ja cadastrado")
    })
    public ResponseEntity<ProfessorResponse> criar(@Valid @RequestBody ProfessorRequest request) {
        ProfessorResponse criado = professorService.criar(request);
        return ResponseEntity.created(URI.create("/api/professores/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um professor", description = "Substitui os dados cadastrais do professor informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Professor nao encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail ja utilizado por outro professor")
    })
    public ResponseEntity<ProfessorResponse> atualizar(
            @Parameter(description = "ID do professor", example = "1") @PathVariable Long id,
            @Valid @RequestBody ProfessorRequest request) {
        return ResponseEntity.ok(professorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um professor",
            description = "A exclusao e bloqueada quando o professor possui horarios vinculados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Professor excluido"),
            @ApiResponse(responseCode = "404", description = "Professor nao encontrado"),
            @ApiResponse(responseCode = "409", description = "Professor possui horarios vinculados")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do professor", example = "1") @PathVariable Long id) {
        professorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/horarios")
    @Operation(summary = "Consulta a grade de horarios do professor",
            description = "Retorna as aulas do professor com disciplina, turma, dia da semana e horarios, "
                    + "ordenadas por dia e hora de inicio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grade retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Professor nao encontrado")
    })
    public ResponseEntity<GradeProfessorResponse> buscarGrade(
            @Parameter(description = "ID do professor", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(professorService.buscarGrade(id));
    }
}

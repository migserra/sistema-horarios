package br.com.unisales.horarios.controller;

import br.com.unisales.horarios.dto.DisciplinaRequest;
import br.com.unisales.horarios.dto.DisciplinaResponse;
import br.com.unisales.horarios.service.DisciplinaService;
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
@RequestMapping("/api/disciplinas")
@Tag(name = "Disciplinas", description = "CRUD de disciplinas")
@SecurityRequirement(name = "basicAuth")
@ApiResponses(@ApiResponse(responseCode = "401", description = "Credenciais ausentes ou invalidas"))
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as disciplinas", description = "Retorna as disciplinas em ordem alfabetica.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<DisciplinaResponse>> listar() {
        return ResponseEntity.ok(disciplinaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma disciplina por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disciplina encontrada"),
            @ApiResponse(responseCode = "404", description = "Disciplina nao encontrada")
    })
    public ResponseEntity<DisciplinaResponse> buscarPorId(
            @Parameter(description = "ID da disciplina", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(disciplinaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma disciplina")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Disciplina criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    public ResponseEntity<DisciplinaResponse> criar(@Valid @RequestBody DisciplinaRequest request) {
        DisciplinaResponse criada = disciplinaService.criar(request);
        return ResponseEntity.created(URI.create("/api/disciplinas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma disciplina")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disciplina atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Disciplina nao encontrada")
    })
    public ResponseEntity<DisciplinaResponse> atualizar(
            @Parameter(description = "ID da disciplina", example = "1") @PathVariable Long id,
            @Valid @RequestBody DisciplinaRequest request) {
        return ResponseEntity.ok(disciplinaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma disciplina",
            description = "A exclusao e bloqueada quando a disciplina possui horarios vinculados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Disciplina excluida"),
            @ApiResponse(responseCode = "404", description = "Disciplina nao encontrada"),
            @ApiResponse(responseCode = "409", description = "Disciplina possui horarios vinculados")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da disciplina", example = "1") @PathVariable Long id) {
        disciplinaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

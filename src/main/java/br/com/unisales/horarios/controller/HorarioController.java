package br.com.unisales.horarios.controller;

import br.com.unisales.horarios.dto.HorarioRequest;
import br.com.unisales.horarios.dto.HorarioResponse;
import br.com.unisales.horarios.service.HorarioService;
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
@RequestMapping("/api/horarios")
@Tag(name = "Horarios", description = "CRUD de horarios de aula com validacao de conflitos")
@SecurityRequirement(name = "basicAuth")
@ApiResponses(@ApiResponse(responseCode = "401", description = "Credenciais ausentes ou invalidas"))
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os horarios", description = "Retorna os horarios ordenados por dia da semana e hora de inicio.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<HorarioResponse>> listar() {
        return ResponseEntity.ok(horarioService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um horario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horario encontrado"),
            @ApiResponse(responseCode = "404", description = "Horario nao encontrado")
    })
    public ResponseEntity<HorarioResponse> buscarPorId(
            @Parameter(description = "ID do horario", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(horarioService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um horario de aula",
            description = "Valida as referencias (professor, disciplina e turma), exige hora de termino posterior "
                    + "a de inicio e impede conflitos de horario para o mesmo professor, turma ou disciplina.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Horario criado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou intervalo de horario invalido"),
            @ApiResponse(responseCode = "404", description = "Professor, disciplina ou turma nao encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito de horario para o professor, turma ou disciplina")
    })
    public ResponseEntity<HorarioResponse> criar(@Valid @RequestBody HorarioRequest request) {
        HorarioResponse criado = horarioService.criar(request);
        return ResponseEntity.created(URI.create("/api/horarios/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um horario de aula",
            description = "Aplica as mesmas validacoes da criacao e ignora o proprio registro na verificacao de conflitos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horario atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou intervalo de horario invalido"),
            @ApiResponse(responseCode = "404", description = "Horario, professor, disciplina ou turma nao encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito de horario para o professor, turma ou disciplina")
    })
    public ResponseEntity<HorarioResponse> atualizar(
            @Parameter(description = "ID do horario", example = "1") @PathVariable Long id,
            @Valid @RequestBody HorarioRequest request) {
        return ResponseEntity.ok(horarioService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um horario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Horario excluido"),
            @ApiResponse(responseCode = "404", description = "Horario nao encontrado")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do horario", example = "1") @PathVariable Long id) {
        horarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

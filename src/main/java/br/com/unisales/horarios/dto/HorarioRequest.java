package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.DiaSemana;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

@Schema(name = "HorarioRequest", description = "Dados para cadastro ou atualizacao de horario de aula")
public record HorarioRequest(

        @Schema(description = "Dia da semana da aula", example = "SEGUNDA")
        @NotNull(message = "O dia da semana e obrigatorio")
        DiaSemana diaSemana,

        @Schema(description = "Hora de inicio da aula", example = "19:00", type = "string", format = "HH:mm")
        @NotNull(message = "A hora de inicio e obrigatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaInicio,

        @Schema(description = "Hora de termino da aula", example = "20:40", type = "string", format = "HH:mm")
        @NotNull(message = "A hora de termino e obrigatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaFim,

        @Schema(description = "Identificador do professor", example = "1")
        @NotNull(message = "O professor e obrigatorio")
        Long professorId,

        @Schema(description = "Identificador da disciplina", example = "1")
        @NotNull(message = "A disciplina e obrigatoria")
        Long disciplinaId,

        @Schema(description = "Identificador da turma", example = "1")
        @NotNull(message = "A turma e obrigatoria")
        Long turmaId
) {
}

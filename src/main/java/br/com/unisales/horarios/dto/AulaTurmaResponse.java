package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.DiaSemana;
import br.com.unisales.horarios.model.Horario;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(name = "AulaTurmaResponse", description = "Aula exibida na grade de uma turma")
public record AulaTurmaResponse(

        @Schema(example = "SEGUNDA")
        DiaSemana diaSemana,

        @Schema(example = "19:00", type = "string")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaInicio,

        @Schema(example = "20:40", type = "string")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaFim,

        @Schema(example = "1")
        Long disciplinaId,

        @Schema(example = "Banco de Dados")
        String disciplinaNome,

        @Schema(example = "1")
        Long professorId,

        @Schema(example = "Ana Souza")
        String professorNome
) {

    public static AulaTurmaResponse de(Horario horario) {
        return new AulaTurmaResponse(
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFim(),
                horario.getDisciplina().getId(),
                horario.getDisciplina().getNome(),
                horario.getProfessor().getId(),
                horario.getProfessor().getNome());
    }
}

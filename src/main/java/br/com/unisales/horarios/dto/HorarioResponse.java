package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.DiaSemana;
import br.com.unisales.horarios.model.Horario;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(name = "HorarioResponse", description = "Dados completos de um horario de aula")
public record HorarioResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "SEGUNDA")
        DiaSemana diaSemana,

        @Schema(example = "19:00", type = "string")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaInicio,

        @Schema(example = "20:40", type = "string")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaFim,

        ProfessorResumo professor,

        DisciplinaResumo disciplina,

        TurmaResumo turma
) {

    public static HorarioResponse de(Horario horario) {
        return new HorarioResponse(
                horario.getId(),
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFim(),
                ProfessorResumo.de(horario.getProfessor()),
                DisciplinaResumo.de(horario.getDisciplina()),
                TurmaResumo.de(horario.getTurma()));
    }
}

package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.DiaSemana;
import br.com.unisales.horarios.model.Horario;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(name = "AulaProfessorResponse", description = "Aula exibida na grade de um professor")
public record AulaProfessorResponse(

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
        Long turmaId,

        @Schema(example = "ES1")
        String turmaIdentificacao,

        @Schema(example = "Engenharia de Software")
        String turmaCurso
) {

    public static AulaProfessorResponse de(Horario horario) {
        return new AulaProfessorResponse(
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFim(),
                horario.getDisciplina().getId(),
                horario.getDisciplina().getNome(),
                horario.getTurma().getId(),
                horario.getTurma().getIdentificacao(),
                horario.getTurma().getCurso());
    }
}

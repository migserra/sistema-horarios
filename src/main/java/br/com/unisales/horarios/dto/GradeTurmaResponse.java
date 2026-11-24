package br.com.unisales.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "GradeTurmaResponse", description = "Grade de horarios de uma turma, ordenada por dia e hora")
public record GradeTurmaResponse(

        @Schema(example = "1")
        Long turmaId,

        @Schema(example = "ES1")
        String turmaIdentificacao,

        @Schema(example = "Engenharia de Software")
        String curso,

        @Schema(example = "2026/1")
        String periodo,

        @Schema(example = "4")
        int totalAulas,

        List<AulaTurmaResponse> aulas
) {
}

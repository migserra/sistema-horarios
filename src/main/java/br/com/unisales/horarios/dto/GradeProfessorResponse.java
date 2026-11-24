package br.com.unisales.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "GradeProfessorResponse", description = "Grade de horarios de um professor, ordenada por dia e hora")
public record GradeProfessorResponse(

        @Schema(example = "1")
        Long professorId,

        @Schema(example = "Ana Souza")
        String professorNome,

        @Schema(example = "3")
        int totalAulas,

        List<AulaProfessorResponse> aulas
) {
}

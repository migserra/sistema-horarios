package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.Turma;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TurmaResponse", description = "Dados de uma turma cadastrada")
public record TurmaResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "ES1")
        String identificacao,

        @Schema(example = "Engenharia de Software")
        String curso,

        @Schema(example = "2026/1")
        String periodo
) {

    public static TurmaResponse de(Turma turma) {
        return new TurmaResponse(
                turma.getId(),
                turma.getIdentificacao(),
                turma.getCurso(),
                turma.getPeriodo());
    }
}

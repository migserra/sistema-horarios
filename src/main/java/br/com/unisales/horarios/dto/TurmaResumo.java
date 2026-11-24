package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.Turma;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TurmaResumo", description = "Resumo da turma exibido em um horario")
public record TurmaResumo(

        @Schema(example = "1")
        Long id,

        @Schema(example = "ES1")
        String identificacao,

        @Schema(example = "Engenharia de Software")
        String curso
) {

    public static TurmaResumo de(Turma turma) {
        return new TurmaResumo(turma.getId(), turma.getIdentificacao(), turma.getCurso());
    }
}

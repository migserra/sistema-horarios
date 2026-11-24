package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.Disciplina;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DisciplinaResumo", description = "Resumo da disciplina exibido em um horario")
public record DisciplinaResumo(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Banco de Dados")
        String nome
) {

    public static DisciplinaResumo de(Disciplina disciplina) {
        return new DisciplinaResumo(disciplina.getId(), disciplina.getNome());
    }
}

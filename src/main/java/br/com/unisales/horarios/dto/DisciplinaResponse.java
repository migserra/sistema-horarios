package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.Disciplina;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DisciplinaResponse", description = "Dados de uma disciplina cadastrada")
public record DisciplinaResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Banco de Dados")
        String nome,

        @Schema(example = "Modelagem relacional, SQL e transacoes")
        String descricao,

        @Schema(example = "80")
        Integer cargaHoraria
) {

    public static DisciplinaResponse de(Disciplina disciplina) {
        return new DisciplinaResponse(
                disciplina.getId(),
                disciplina.getNome(),
                disciplina.getDescricao(),
                disciplina.getCargaHoraria());
    }
}

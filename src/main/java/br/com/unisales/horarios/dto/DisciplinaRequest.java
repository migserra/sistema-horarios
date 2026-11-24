package br.com.unisales.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(name = "DisciplinaRequest", description = "Dados para cadastro ou atualizacao de disciplina")
public record DisciplinaRequest(

        @Schema(description = "Nome da disciplina", example = "Banco de Dados")
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String nome,

        @Schema(description = "Descricao da disciplina", example = "Modelagem relacional, SQL e transacoes")
        @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
        String descricao,

        @Schema(description = "Carga horaria em horas", example = "80")
        @NotNull(message = "A carga horaria e obrigatoria")
        @Positive(message = "A carga horaria deve ser maior que zero")
        @Max(value = 1000, message = "A carga horaria deve ser no maximo 1000 horas")
        Integer cargaHoraria
) {
}

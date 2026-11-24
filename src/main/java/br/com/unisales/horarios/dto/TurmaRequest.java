package br.com.unisales.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "TurmaRequest", description = "Dados para cadastro ou atualizacao de turma")
public record TurmaRequest(

        @Schema(description = "Identificacao da turma", example = "ES1")
        @NotBlank(message = "A identificacao e obrigatoria")
        @Size(max = 50, message = "A identificacao deve ter no maximo 50 caracteres")
        String identificacao,

        @Schema(description = "Curso ao qual a turma pertence", example = "Engenharia de Software")
        @NotBlank(message = "O curso e obrigatorio")
        @Size(max = 120, message = "O curso deve ter no maximo 120 caracteres")
        String curso,

        @Schema(description = "Periodo letivo", example = "2026/1")
        @NotBlank(message = "O periodo e obrigatorio")
        @Size(max = 20, message = "O periodo deve ter no maximo 20 caracteres")
        String periodo
) {
}

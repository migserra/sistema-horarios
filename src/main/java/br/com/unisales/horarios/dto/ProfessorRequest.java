package br.com.unisales.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ProfessorRequest", description = "Dados para cadastro ou atualizacao de professor")
public record ProfessorRequest(

        @Schema(description = "Nome completo do professor", example = "Ana Souza")
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String nome,

        @Schema(description = "E-mail institucional ou pessoal", example = "ana.souza@unisales.edu.br")
        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "Informe um e-mail valido")
        @Size(max = 160, message = "O e-mail deve ter no maximo 160 caracteres")
        String email,

        @Schema(description = "Telefone de contato", example = "(27) 99999-0000")
        @NotBlank(message = "O telefone e obrigatorio")
        @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres")
        String telefone
) {
}

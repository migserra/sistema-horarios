package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.Professor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProfessorResumo", description = "Resumo do professor exibido em um horario")
public record ProfessorResumo(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Ana Souza")
        String nome,

        @Schema(example = "ana.souza@unisales.edu.br")
        String email
) {

    public static ProfessorResumo de(Professor professor) {
        return new ProfessorResumo(professor.getId(), professor.getNome(), professor.getEmail());
    }
}

package br.com.unisales.horarios.dto;

import br.com.unisales.horarios.model.Professor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProfessorResponse", description = "Dados de um professor cadastrado")
public record ProfessorResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Ana Souza")
        String nome,

        @Schema(example = "ana.souza@unisales.edu.br")
        String email,

        @Schema(example = "(27) 99999-0000")
        String telefone
) {

    public static ProfessorResponse de(Professor professor) {
        return new ProfessorResponse(
                professor.getId(),
                professor.getNome(),
                professor.getEmail(),
                professor.getTelefone());
    }
}

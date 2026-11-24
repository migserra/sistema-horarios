package br.com.unisales.horarios.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/**
 * Dias da semana em que uma aula pode ser alocada.
 * A ordem de declaracao e utilizada para ordenar as grades de horario.
 */
public enum DiaSemana {

    SEGUNDA("Segunda-feira"),
    TERCA("Terça-feira"),
    QUARTA("Quarta-feira"),
    QUINTA("Quinta-feira"),
    SEXTA("Sexta-feira"),
    SABADO("Sábado");

    private final String descricao;

    DiaSemana(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Permite receber o dia em maiusculas ou minusculas no JSON (ex.: "segunda").
     */
    @JsonCreator
    public static DiaSemana deTexto(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return DiaSemana.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Dia da semana invalido: '" + valor
                            + "'. Valores aceitos: SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO.");
        }
    }
}

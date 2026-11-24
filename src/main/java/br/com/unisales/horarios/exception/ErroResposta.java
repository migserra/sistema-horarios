package br.com.unisales.horarios.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

/**
 * Formato padrao de erro retornado pela API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResposta(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {

    public static ErroResposta de(HttpStatus status, String mensagem, String caminho) {
        return new ErroResposta(Instant.now(), status.value(), status.getReasonPhrase(), mensagem, caminho, null);
    }

    public static ErroResposta deValidacao(HttpStatus status, String mensagem, String caminho,
                                           Map<String, String> campos) {
        return new ErroResposta(Instant.now(), status.value(), status.getReasonPhrase(), mensagem, caminho, campos);
    }
}

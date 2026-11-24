package br.com.unisales.horarios.exception;

/**
 * Representa a violacao de uma regra de negocio que impede a operacao,
 * como exclusao de um registro com vinculos ou duplicidade de dados.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}

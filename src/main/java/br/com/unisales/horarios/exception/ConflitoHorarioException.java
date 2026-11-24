package br.com.unisales.horarios.exception;

/**
 * Lancada quando o horario informado conflita com outro ja cadastrado
 * para o mesmo professor, turma ou disciplina.
 */
public class ConflitoHorarioException extends RegraDeNegocioException {

    public ConflitoHorarioException(String mensagem) {
        super(mensagem);
    }
}

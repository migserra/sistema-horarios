package br.com.unisales.horarios.exception;

/**
 * Lancada quando o intervalo de horario e invalido, por exemplo
 * quando a hora de termino nao e posterior a hora de inicio.
 */
public class HorarioInvalidoException extends RuntimeException {

    public HorarioInvalidoException(String mensagem) {
        super(mensagem);
    }
}

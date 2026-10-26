package pe.marathon.inventario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConteoInvalidoException extends RuntimeException {
    public ConteoInvalidoException(String mensaje) {
        super(mensaje);
    }
}

package co.edu.autonoma.adopcionmascotas.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// los servicios lanzan esta excepcion y GlobalExceptionHandler la convierte en el JSON de error
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus estado;
    private final String codigo;

    public ApiException(HttpStatus estado, String codigo, String mensaje) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
    }

    public static ApiException noAutenticado(String mensaje) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", mensaje);
    }

    public static ApiException sinPermiso(String mensaje) {
        return new ApiException(HttpStatus.FORBIDDEN, "SIN_PERMISO", mensaje);
    }

    public static ApiException noEncontrado(String mensaje) {
        return new ApiException(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", mensaje);
    }

    // el codigo es la regla incumplida (RN-01 RN-02 RN-05) o CONFLICTO si no aplica una regla
    public static ApiException conflicto(String codigo, String mensaje) {
        return new ApiException(HttpStatus.CONFLICT, codigo, mensaje);
    }
}

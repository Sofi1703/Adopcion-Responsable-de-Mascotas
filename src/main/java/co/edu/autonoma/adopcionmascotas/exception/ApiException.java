package co.edu.autonoma.adopcionmascotas.exception;

import org.springframework.http.HttpStatus;

// los servicios lanzan esta excepcion y GlobalExceptionHandler la convierte en el JSON de error
public class ApiException extends RuntimeException {

    private final HttpStatus estado;
    private final String codigo;

    public ApiException(HttpStatus estado, String codigo, String mensaje) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
    }

    public HttpStatus getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
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
}

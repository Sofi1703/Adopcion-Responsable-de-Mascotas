package co.edu.autonoma.adopcionmascotas.exception;

import co.edu.autonoma.adopcionmascotas.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> manejarApi(ApiException ex, HttpServletRequest request) {
        return responder(ex.getEstado(), ex.getCodigo(), ex.getMessage(), request);
    }

    // por ejemplo ?estado=XYZ o un id que no es numerico
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoInvalido(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String mensaje = "El valor '" + ex.getValue() + "' no es valido para el parametro '" + ex.getName() + "'";
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", mensaje, request);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String codigo, String mensaje,
            HttpServletRequest request) {
        ErrorResponse cuerpo = new ErrorResponse(estado.value(), estado.getReasonPhrase(), codigo, mensaje,
                request.getRequestURI(), LocalDateTime.now());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}

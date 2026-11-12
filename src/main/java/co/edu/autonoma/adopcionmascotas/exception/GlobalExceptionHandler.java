package co.edu.autonoma.adopcionmascotas.exception;

import co.edu.autonoma.adopcionmascotas.dto.ErrorCampo;
import co.edu.autonoma.adopcionmascotas.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> manejarApi(ApiException ex, HttpServletRequest request) {
        log.info("{} {} -> {} {}", request.getMethod(), request.getRequestURI(), ex.getEstado().value(), ex.getCodigo());
        return responder(ex.getEstado(), ex.getCodigo(), ex.getMessage(), request, null);
    }

    // por ejemplo ?estado=XYZ o un id que no es numerico
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoInvalido(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String mensaje = "El valor '" + ex.getValue() + "' no es valido para el parametro '" + ex.getName() + "'";
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", mensaje, request, null);
    }

    // el body no se pudo leer (JSON mal formado o un estado que no existe)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpoInvalido(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION",
                "El cuerpo de la peticion no es un JSON valido o tiene un valor no permitido", request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErrorCampo(e.getField(), e.getDefaultMessage()))
                .toList();

        // RN-06 si lo unico que fallo fue la imagen se informa esa regla
        boolean soloImagen = errores.stream().allMatch(e -> e.getCampo().equals("imagenUrl"));
        String codigo = soloImagen ? "RN-06" : "VALIDACION";
        return responder(HttpStatus.BAD_REQUEST, codigo, "Hay datos invalidos en la peticion", request, errores);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String codigo, String mensaje,
            HttpServletRequest request, List<ErrorCampo> errores) {
        ErrorResponse cuerpo = ErrorResponse.builder()
                .status(estado.value())
                .error(estado.getReasonPhrase())
                .codigo(codigo)
                .mensaje(mensaje)
                .ruta(request.getRequestURI())
                .fecha(LocalDateTime.now())
                .errores(errores)
                .build();
        return ResponseEntity.status(estado).body(cuerpo);
    }
}

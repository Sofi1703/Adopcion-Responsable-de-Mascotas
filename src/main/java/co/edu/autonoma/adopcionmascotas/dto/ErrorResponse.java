package co.edu.autonoma.adopcionmascotas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

// formato unico de error (el contrato esta en la carpeta docs)
public record ErrorResponse(
        int status,
        String error,
        String codigo,
        String mensaje,
        String ruta,
        LocalDateTime fecha,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<ErrorCampo> errores) {
}

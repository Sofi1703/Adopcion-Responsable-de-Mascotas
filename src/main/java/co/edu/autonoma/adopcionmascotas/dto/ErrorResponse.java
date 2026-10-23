package co.edu.autonoma.adopcionmascotas.dto;

import java.time.LocalDateTime;

// formato unico de error (ver docs/api-endpoints.md)
public record ErrorResponse(
        int status,
        String error,
        String codigo,
        String mensaje,
        String ruta,
        LocalDateTime fecha) {
}

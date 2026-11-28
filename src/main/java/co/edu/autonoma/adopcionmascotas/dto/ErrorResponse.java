package co.edu.autonoma.adopcionmascotas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// formato unico de error (el contrato esta en la carpeta docs)
@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String codigo;
    private String mensaje;
    private String ruta;
    private LocalDateTime fecha;

    // solo aparece en los errores de validacion
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ErrorCampo> errores;
}

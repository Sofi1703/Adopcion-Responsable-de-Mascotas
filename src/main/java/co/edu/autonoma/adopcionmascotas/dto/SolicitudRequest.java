package co.edu.autonoma.adopcionmascotas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitudRequest(
        @NotNull(message = "es obligatorio") Long mascotaId,
        @NotBlank(message = "es obligatorio") @Size(max = 100, message = "maximo 100 caracteres") String solicitanteNombre,
        @NotBlank(message = "es obligatorio") @Email(message = "no tiene formato de correo")
        @Size(max = 150, message = "maximo 150 caracteres") String solicitanteCorreo,
        @NotBlank(message = "es obligatorio") @Size(max = 30, message = "maximo 30 caracteres") String telefono) {
}

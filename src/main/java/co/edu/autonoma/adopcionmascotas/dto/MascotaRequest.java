package co.edu.autonoma.adopcionmascotas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// sirve para crear (POST) y reemplazar la ficha (PUT) y el estado no se recibe porque solo cambia por RN-03
public record MascotaRequest(
        @NotBlank(message = "es obligatorio") @Size(max = 100, message = "maximo 100 caracteres") String nombre,
        @NotBlank(message = "es obligatoria") @Size(max = 50, message = "maximo 50 caracteres") String especie,
        @Size(max = 100, message = "maximo 100 caracteres") String raza,
        String descripcion,
        // RN-06 la imagen es opcional pero si viene debe ser una URL http o https
        @Size(max = 500, message = "maximo 500 caracteres")
        @Pattern(regexp = "^https?://.+", message = "debe ser una URL que empiece por http:// o https://") String imagenUrl) {
}

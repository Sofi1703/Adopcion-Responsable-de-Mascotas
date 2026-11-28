package co.edu.autonoma.adopcionmascotas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// sirve para crear (POST) y reemplazar la ficha (PUT) y el estado no se recibe porque solo cambia por RN-03
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MascotaRequest {

    @NotBlank(message = "es obligatorio")
    @Size(max = 100, message = "maximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "es obligatoria")
    @Size(max = 50, message = "maximo 50 caracteres")
    private String especie;

    @Size(max = 100, message = "maximo 100 caracteres")
    private String raza;

    private String descripcion;

    // RN-06 la imagen es opcional pero si viene debe ser una URL http o https
    @Size(max = 500, message = "maximo 500 caracteres")
    @Pattern(regexp = "^https?://.+", message = "debe ser una URL que empiece por http:// o https://")
    private String imagenUrl;
}

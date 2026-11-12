package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// ficha publica de la mascota (no lleva ningun dato de contacto)
@Getter
@Builder
@AllArgsConstructor
public class MascotaResponse {

    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private String descripcion;
    private EstadoMascota estado;
    private String imagenUrl;

    public static MascotaResponse desde(Mascota mascota) {
        return MascotaResponse.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .especie(mascota.getEspecie())
                .raza(mascota.getRaza())
                .descripcion(mascota.getDescripcion())
                .estado(mascota.getEstado())
                .imagenUrl(mascota.getImagenUrl())
                .build();
    }
}

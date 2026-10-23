package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.Mascota;

// ficha publica de la mascota (no lleva ningun dato de contacto)
public record MascotaResponse(
        Long id,
        String nombre,
        String especie,
        String raza,
        String descripcion,
        EstadoMascota estado,
        String imagenUrl) {

    public static MascotaResponse desde(Mascota mascota) {
        return new MascotaResponse(
                mascota.getId(),
                mascota.getNombre(),
                mascota.getEspecie(),
                mascota.getRaza(),
                mascota.getDescripcion(),
                mascota.getEstado(),
                mascota.getImagenUrl());
    }
}

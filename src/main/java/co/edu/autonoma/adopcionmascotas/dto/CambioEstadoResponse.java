package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import java.util.List;

// muestra el efecto del cambio (RN-03 cambia la mascota y RN-04 cierra otras solicitudes)
public record CambioEstadoResponse(
        Long id,
        EstadoSolicitud estado,
        Long mascotaId,
        EstadoMascota mascotaEstado,
        List<Long> solicitudesCerradas) {
}

package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.Solicitud;
import java.time.LocalDateTime;

// version para listados (sin correo ni telefono del solicitante)
public record SolicitudResumenResponse(
        Long id,
        Long mascotaId,
        String mascotaNombre,
        String solicitanteNombre,
        EstadoSolicitud estado,
        LocalDateTime creadaEn,
        LocalDateTime retiradaEn) {

    public static SolicitudResumenResponse desde(Solicitud solicitud) {
        return new SolicitudResumenResponse(
                solicitud.getId(),
                solicitud.getMascota().getId(),
                solicitud.getMascota().getNombre(),
                solicitud.getSolicitanteNombre(),
                solicitud.getEstado(),
                solicitud.getCreadaEn(),
                solicitud.getRetiradaEn());
    }
}

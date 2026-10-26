package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.HistorialSolicitud;
import co.edu.autonoma.adopcionmascotas.model.Solicitud;
import java.time.LocalDateTime;
import java.util.List;

// version completa con datos de contacto e historial (solo para quien tiene permiso)
public record SolicitudDetalleResponse(
        Long id,
        Long mascotaId,
        String mascotaNombre,
        String solicitanteNombre,
        String solicitanteCorreo,
        String telefono,
        EstadoSolicitud estado,
        LocalDateTime creadaEn,
        LocalDateTime retiradaEn,
        List<HistorialResponse> historial) {

    public static SolicitudDetalleResponse desde(Solicitud solicitud, List<HistorialSolicitud> historial) {
        return new SolicitudDetalleResponse(
                solicitud.getId(),
                solicitud.getMascota().getId(),
                solicitud.getMascota().getNombre(),
                solicitud.getSolicitanteNombre(),
                solicitud.getSolicitanteCorreo(),
                solicitud.getTelefono(),
                solicitud.getEstado(),
                solicitud.getCreadaEn(),
                solicitud.getRetiradaEn(),
                historial.stream().map(HistorialResponse::desde).toList());
    }
}

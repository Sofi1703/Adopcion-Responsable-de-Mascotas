package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.HistorialSolicitud;
import co.edu.autonoma.adopcionmascotas.model.Solicitud;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// version completa con datos de contacto e historial (solo para quien tiene permiso)
@Getter
@Builder
@AllArgsConstructor
public class SolicitudDetalleResponse {

    private Long id;
    private Long mascotaId;
    private String mascotaNombre;
    private String solicitanteNombre;
    private String solicitanteCorreo;
    private String telefono;
    private EstadoSolicitud estado;
    private LocalDateTime creadaEn;
    private LocalDateTime retiradaEn;
    private List<HistorialResponse> historial;

    public static SolicitudDetalleResponse desde(Solicitud solicitud, List<HistorialSolicitud> historial) {
        return SolicitudDetalleResponse.builder()
                .id(solicitud.getId())
                .mascotaId(solicitud.getMascota().getId())
                .mascotaNombre(solicitud.getMascota().getNombre())
                .solicitanteNombre(solicitud.getSolicitanteNombre())
                .solicitanteCorreo(solicitud.getSolicitanteCorreo())
                .telefono(solicitud.getTelefono())
                .estado(solicitud.getEstado())
                .creadaEn(solicitud.getCreadaEn())
                .retiradaEn(solicitud.getRetiradaEn())
                .historial(historial.stream().map(HistorialResponse::desde).toList())
                .build();
    }
}

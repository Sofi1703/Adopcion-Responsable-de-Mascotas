package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.Solicitud;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// version para listados (sin correo ni telefono del solicitante)
@Getter
@Builder
@AllArgsConstructor
public class SolicitudResumenResponse {

    private Long id;
    private Long mascotaId;
    private String mascotaNombre;
    private String solicitanteNombre;
    private EstadoSolicitud estado;
    private LocalDateTime creadaEn;
    private LocalDateTime retiradaEn;

    public static SolicitudResumenResponse desde(Solicitud solicitud) {
        return SolicitudResumenResponse.builder()
                .id(solicitud.getId())
                .mascotaId(solicitud.getMascota().getId())
                .mascotaNombre(solicitud.getMascota().getNombre())
                .solicitanteNombre(solicitud.getSolicitanteNombre())
                .estado(solicitud.getEstado())
                .creadaEn(solicitud.getCreadaEn())
                .retiradaEn(solicitud.getRetiradaEn())
                .build();
    }
}

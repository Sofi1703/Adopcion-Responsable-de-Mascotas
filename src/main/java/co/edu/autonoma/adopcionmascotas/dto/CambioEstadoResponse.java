package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// muestra el efecto del cambio (RN-03 cambia la mascota y RN-04 cierra otras solicitudes)
@Getter
@Builder
@AllArgsConstructor
public class CambioEstadoResponse {

    private Long id;
    private EstadoSolicitud estado;
    private Long mascotaId;
    private EstadoMascota mascotaEstado;
    private List<Long> solicitudesCerradas;
}

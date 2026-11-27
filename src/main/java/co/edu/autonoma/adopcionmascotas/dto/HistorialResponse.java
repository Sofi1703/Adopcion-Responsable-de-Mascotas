package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.HistorialSolicitud;
import co.edu.autonoma.adopcionmascotas.model.TipoHistorial;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HistorialResponse {

    private TipoHistorial tipo;
    private EstadoSolicitud estadoAnterior;
    private EstadoSolicitud estadoNuevo;
    private String actor;
    private LocalDateTime fecha;

    public static HistorialResponse desde(HistorialSolicitud evento) {
        return HistorialResponse.builder()
                .tipo(evento.getTipo())
                .estadoAnterior(evento.getEstadoAnterior())
                .estadoNuevo(evento.getEstadoNuevo())
                .actor(evento.getActor())
                .fecha(evento.getFecha())
                .build();
    }
}

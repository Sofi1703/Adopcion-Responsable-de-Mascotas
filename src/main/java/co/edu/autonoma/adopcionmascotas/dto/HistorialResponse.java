package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.HistorialSolicitud;
import co.edu.autonoma.adopcionmascotas.model.TipoHistorial;
import java.time.LocalDateTime;

public record HistorialResponse(
        TipoHistorial tipo,
        EstadoSolicitud estadoAnterior,
        EstadoSolicitud estadoNuevo,
        String actor,
        LocalDateTime fecha) {

    public static HistorialResponse desde(HistorialSolicitud evento) {
        return new HistorialResponse(
                evento.getTipo(),
                evento.getEstadoAnterior(),
                evento.getEstadoNuevo(),
                evento.getActor(),
                evento.getFecha());
    }
}

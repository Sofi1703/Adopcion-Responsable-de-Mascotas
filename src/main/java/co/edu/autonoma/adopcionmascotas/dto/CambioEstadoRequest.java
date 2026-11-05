package co.edu.autonoma.adopcionmascotas.dto;

import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(@NotNull(message = "es obligatorio") EstadoSolicitud estado) {
}

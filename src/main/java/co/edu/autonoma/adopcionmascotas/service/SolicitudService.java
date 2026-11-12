package co.edu.autonoma.adopcionmascotas.service;

import co.edu.autonoma.adopcionmascotas.dto.SolicitudDetalleResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudRequest;
import co.edu.autonoma.adopcionmascotas.exception.ApiException;
import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.HistorialSolicitud;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import co.edu.autonoma.adopcionmascotas.model.Solicitud;
import co.edu.autonoma.adopcionmascotas.model.TipoHistorial;
import co.edu.autonoma.adopcionmascotas.repository.HistorialSolicitudRepository;
import co.edu.autonoma.adopcionmascotas.repository.MascotaRepository;
import co.edu.autonoma.adopcionmascotas.repository.SolicitudRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudService {

    // RN-02 estados en los que una solicitud sigue vigente
    private static final List<EstadoSolicitud> ESTADOS_ACTIVOS =
            List.of(EstadoSolicitud.RECIBIDA, EstadoSolicitud.EN_REVISION);

    private final SolicitudRepository solicitudRepository;
    private final MascotaRepository mascotaRepository;
    private final HistorialSolicitudRepository historialRepository;

    // el solicitante no tiene cuenta y por eso se identifica con los datos del body
    @Transactional
    public SolicitudDetalleResponse crear(SolicitudRequest request) {
        // se bloquea la mascota para que una aprobacion en curso no se cruce con esta solicitud
        Mascota mascota = mascotaRepository.buscarConBloqueo(request.getMascotaId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe la mascota " + request.getMascotaId()));

        // RN-05
        if (mascota.getEstado() == EstadoMascota.ADOPTADA) {
            throw ApiException.conflicto("RN-05", "La mascota ya fue adoptada y no recibe nuevas solicitudes");
        }

        // RN-01
        String correo = request.getSolicitanteCorreo().trim();
        if (solicitudRepository.existeActiva(mascota.getId(), correo, ESTADOS_ACTIVOS)) {
            throw ApiException.conflicto("RN-01", "Ya tienes una solicitud activa para esta mascota");
        }

        Solicitud solicitud = solicitudRepository.save(Solicitud.builder()
                .mascota(mascota)
                .solicitanteCorreo(correo)
                .solicitanteNombre(request.getSolicitanteNombre().trim())
                .telefono(request.getTelefono().trim())
                .build());
        registrar(solicitud, TipoHistorial.CREACION, null, EstadoSolicitud.RECIBIDA, correo);

        return detalle(solicitud);
    }

    private void registrar(Solicitud solicitud, TipoHistorial tipo, EstadoSolicitud anterior,
            EstadoSolicitud nuevo, String actor) {
        historialRepository.save(HistorialSolicitud.builder()
                .solicitud(solicitud)
                .tipo(tipo)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .actor(actor)
                .build());
    }

    private SolicitudDetalleResponse detalle(Solicitud solicitud) {
        return SolicitudDetalleResponse.desde(solicitud,
                historialRepository.findBySolicitudIdOrderByFechaAscIdAsc(solicitud.getId()));
    }
}

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SolicitudService {

    // estados en los que una solicitud sigue vigente
    private static final List<EstadoSolicitud> ESTADOS_ACTIVOS =
            List.of(EstadoSolicitud.RECIBIDA, EstadoSolicitud.EN_REVISION);

    private final SolicitudRepository solicitudRepository;
    private final MascotaRepository mascotaRepository;
    private final HistorialSolicitudRepository historialRepository;

    public SolicitudService(SolicitudRepository solicitudRepository,
            MascotaRepository mascotaRepository,
            HistorialSolicitudRepository historialRepository) {
        this.solicitudRepository = solicitudRepository;
        this.mascotaRepository = mascotaRepository;
        this.historialRepository = historialRepository;
    }

    // el solicitante no tiene cuenta y por eso se identifica con los datos del body
    @Transactional
    public SolicitudDetalleResponse crear(SolicitudRequest request) {
        // se bloquea la mascota para que dos solicitudes sobre la misma mascota se atiendan una tras otra
        Mascota mascota = mascotaRepository.buscarConBloqueo(request.mascotaId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe la mascota " + request.mascotaId()));

        // RN-05
        if (mascota.getEstado() == EstadoMascota.ADOPTADA) {
            throw ApiException.conflicto("RN-05", "La mascota ya fue adoptada y no recibe nuevas solicitudes");
        }

        // RN-01
        String correo = request.solicitanteCorreo().trim();
        if (solicitudRepository.existeActiva(mascota.getId(), correo, ESTADOS_ACTIVOS)) {
            throw ApiException.conflicto("RN-01", "Ya tienes una solicitud activa para esta mascota");
        }

        Solicitud solicitud = solicitudRepository.save(new Solicitud(
                mascota, correo, request.solicitanteNombre().trim(), request.telefono().trim()));
        historialRepository.save(new HistorialSolicitud(
                solicitud, TipoHistorial.CREACION, null, EstadoSolicitud.RECIBIDA, correo));

        return SolicitudDetalleResponse.desde(solicitud,
                historialRepository.findBySolicitudIdOrderByFechaAscIdAsc(solicitud.getId()));
    }
}

package co.edu.autonoma.adopcionmascotas.service;

import co.edu.autonoma.adopcionmascotas.dto.SolicitudDetalleResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudResumenResponse;
import co.edu.autonoma.adopcionmascotas.exception.ApiException;
import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.HistorialSolicitud;
import co.edu.autonoma.adopcionmascotas.model.Solicitud;
import co.edu.autonoma.adopcionmascotas.repository.HistorialSolicitudRepository;
import co.edu.autonoma.adopcionmascotas.repository.SolicitudRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final HistorialSolicitudRepository historialRepository;
    private final AutorizacionService autorizacion;

    public SolicitudService(SolicitudRepository solicitudRepository,
            HistorialSolicitudRepository historialRepository,
            AutorizacionService autorizacion) {
        this.solicitudRepository = solicitudRepository;
        this.historialRepository = historialRepository;
        this.autorizacion = autorizacion;
    }

    // listado solo para el personal y sin datos de contacto (los filtros son opcionales)
    public List<SolicitudResumenResponse> listar(Identidad identidad, Long mascotaId, EstadoSolicitud estado) {
        autorizacion.exigirPersonal(identidad);

        List<Solicitud> solicitudes;
        if (mascotaId != null && estado != null) {
            solicitudes = solicitudRepository.findByMascotaIdAndEstadoOrderByCreadaEnDesc(mascotaId, estado);
        } else if (mascotaId != null) {
            solicitudes = solicitudRepository.findByMascotaIdOrderByCreadaEnDesc(mascotaId);
        } else if (estado != null) {
            solicitudes = solicitudRepository.findByEstadoOrderByCreadaEnDesc(estado);
        } else {
            solicitudes = solicitudRepository.findAllByOrderByCreadaEnDesc();
        }
        return solicitudes.stream().map(SolicitudResumenResponse::desde).toList();
    }

    // el personal ve cualquier solicitud y un solicitante solo la suya (se compara por correo)
    public SolicitudDetalleResponse obtener(Identidad identidad, Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la solicitud " + id));

        boolean esDuenio = solicitud.getSolicitanteCorreo().equalsIgnoreCase(identidad.usuario());
        if (!identidad.esPersonal() && !esDuenio) {
            throw ApiException.sinPermiso("Solo puedes consultar tus propias solicitudes");
        }

        List<HistorialSolicitud> historial = historialRepository.findBySolicitudIdOrderByFechaAscIdAsc(id);
        return SolicitudDetalleResponse.desde(solicitud, historial);
    }
}

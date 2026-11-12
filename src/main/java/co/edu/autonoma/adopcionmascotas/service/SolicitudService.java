package co.edu.autonoma.adopcionmascotas.service;

import co.edu.autonoma.adopcionmascotas.dto.CambioEstadoRequest;
import co.edu.autonoma.adopcionmascotas.dto.CambioEstadoResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudDetalleResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudRequest;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudResumenResponse;
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
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudService {

    private static final String ACTOR_SISTEMA = "SISTEMA";

    // RN-02 estados en los que una solicitud sigue vigente
    private static final List<EstadoSolicitud> ESTADOS_ACTIVOS =
            List.of(EstadoSolicitud.RECIBIDA, EstadoSolicitud.EN_REVISION);

    // RN-02 unicas transiciones validas y los estados finales no tienen salida
    private static final Map<EstadoSolicitud, List<EstadoSolicitud>> TRANSICIONES = Map.of(
            EstadoSolicitud.RECIBIDA, List.of(EstadoSolicitud.EN_REVISION, EstadoSolicitud.RECHAZADA),
            EstadoSolicitud.EN_REVISION, List.of(EstadoSolicitud.APROBADA, EstadoSolicitud.RECHAZADA));

    private final SolicitudRepository solicitudRepository;
    private final MascotaRepository mascotaRepository;
    private final HistorialSolicitudRepository historialRepository;
    private final AutorizacionService autorizacion;

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

        boolean esDuenio = solicitud.getSolicitanteCorreo().equalsIgnoreCase(identidad.getUsuario());
        if (!identidad.esPersonal() && !esDuenio) {
            throw ApiException.sinPermiso("Solo puedes consultar tus propias solicitudes");
        }
        return detalle(solicitud);
    }

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

    // RN-02 RN-03 y RN-04 se aplican en la misma transaccion (o se guarda todo o nada)
    @Transactional
    public CambioEstadoResponse cambiarEstado(Identidad identidad, Long id, CambioEstadoRequest request) {
        autorizacion.exigirPersonal(identidad);
        EstadoSolicitud nuevoEstado = request.getEstado();

        Solicitud solicitud = cargarConMascotaBloqueada(id);
        Mascota mascota = solicitud.getMascota();

        // RN-02
        List<EstadoSolicitud> permitidos = TRANSICIONES.getOrDefault(solicitud.getEstado(), List.of());
        if (solicitud.getRetiradaEn() != null || !permitidos.contains(nuevoEstado)) {
            String motivo = solicitud.getRetiradaEn() != null
                    ? "La solicitud fue retirada y no puede cambiar de estado"
                    : "No se puede pasar una solicitud de " + solicitud.getEstado() + " a " + nuevoEstado;
            throw ApiException.conflicto("RN-02", motivo);
        }

        // RN-05 si otra aprobacion ya adopto a la mascota esta no puede aprobarse
        if (nuevoEstado == EstadoSolicitud.APROBADA && mascota.getEstado() == EstadoMascota.ADOPTADA) {
            throw ApiException.conflicto("RN-05", "La mascota ya fue adoptada");
        }

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(nuevoEstado);
        registrar(solicitud, TipoHistorial.CAMBIO_ESTADO, estadoAnterior, nuevoEstado, identidad.getUsuario());

        List<Long> cerradas = List.of();
        if (nuevoEstado == EstadoSolicitud.APROBADA) {
            // RN-03
            mascota.setEstado(EstadoMascota.ADOPTADA);
            // RN-04
            cerradas = cerrarOtrasSolicitudes(mascota, solicitud);
        }

        return CambioEstadoResponse.builder()
                .id(solicitud.getId())
                .estado(solicitud.getEstado())
                .mascotaId(mascota.getId())
                .mascotaEstado(mascota.getEstado())
                .solicitudesCerradas(cerradas)
                .build();
    }

    // RN-04 las demas solicitudes activas de la mascota pasan a RECHAZADA con actor SISTEMA
    private List<Long> cerrarOtrasSolicitudes(Mascota mascota, Solicitud aprobada) {
        List<Solicitud> otras = solicitudRepository.buscarActivasDeMascota(
                mascota.getId(), aprobada.getId(), ESTADOS_ACTIVOS);
        for (Solicitud otra : otras) {
            EstadoSolicitud estadoAnterior = otra.getEstado();
            otra.setEstado(EstadoSolicitud.RECHAZADA);
            registrar(otra, TipoHistorial.CIERRE_AUTOMATICO, estadoAnterior, EstadoSolicitud.RECHAZADA, ACTOR_SISTEMA);
        }
        return otras.stream().map(Solicitud::getId).toList();
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

    // primero se bloquea la mascota y despues se lee la solicitud para no trabajar con datos viejos
    private Solicitud cargarConMascotaBloqueada(Long id) {
        Long mascotaId = solicitudRepository.buscarMascotaId(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la solicitud " + id));
        mascotaRepository.buscarConBloqueo(mascotaId);
        return solicitudRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la solicitud " + id));
    }

    private SolicitudDetalleResponse detalle(Solicitud solicitud) {
        return SolicitudDetalleResponse.desde(solicitud,
                historialRepository.findBySolicitudIdOrderByFechaAscIdAsc(solicitud.getId()));
    }
}

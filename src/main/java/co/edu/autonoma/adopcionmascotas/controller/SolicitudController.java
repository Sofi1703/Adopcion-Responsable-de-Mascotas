package co.edu.autonoma.adopcionmascotas.controller;

import co.edu.autonoma.adopcionmascotas.dto.SolicitudDetalleResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudResumenResponse;
import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.service.AutorizacionService;
import co.edu.autonoma.adopcionmascotas.service.SolicitudService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final AutorizacionService autorizacion;

    public SolicitudController(SolicitudService solicitudService, AutorizacionService autorizacion) {
        this.solicitudService = solicitudService;
        this.autorizacion = autorizacion;
    }

    // las cabeceras no son obligatorias para Spring (el 401 lo da AutorizacionService con el JSON de error)
    @GetMapping
    public List<SolicitudResumenResponse> listar(
            @RequestHeader(name = AutorizacionService.CABECERA_ROL, required = false) String rol,
            @RequestHeader(name = AutorizacionService.CABECERA_USUARIO, required = false) String usuario,
            @RequestParam(required = false) Long mascotaId,
            @RequestParam(required = false) EstadoSolicitud estado) {
        return solicitudService.listar(autorizacion.identificar(rol, usuario), mascotaId, estado);
    }

    @GetMapping("/{id}")
    public SolicitudDetalleResponse obtener(
            @RequestHeader(name = AutorizacionService.CABECERA_ROL, required = false) String rol,
            @RequestHeader(name = AutorizacionService.CABECERA_USUARIO, required = false) String usuario,
            @PathVariable Long id) {
        return solicitudService.obtener(autorizacion.identificar(rol, usuario), id);
    }
}

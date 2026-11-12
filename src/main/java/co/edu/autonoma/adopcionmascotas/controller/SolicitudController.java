package co.edu.autonoma.adopcionmascotas.controller;

import co.edu.autonoma.adopcionmascotas.dto.CambioEstadoRequest;
import co.edu.autonoma.adopcionmascotas.dto.CambioEstadoResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudDetalleResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudResumenResponse;
import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.service.AutorizacionService;
import co.edu.autonoma.adopcionmascotas.service.SolicitudService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final AutorizacionService autorizacion;

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

    @PatchMapping("/{id}/estado")
    public CambioEstadoResponse cambiarEstado(
            @RequestHeader(name = AutorizacionService.CABECERA_ROL, required = false) String rol,
            @RequestHeader(name = AutorizacionService.CABECERA_USUARIO, required = false) String usuario,
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoRequest request) {
        return solicitudService.cambiarEstado(autorizacion.identificar(rol, usuario), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(
            @RequestHeader(name = AutorizacionService.CABECERA_ROL, required = false) String rol,
            @RequestHeader(name = AutorizacionService.CABECERA_USUARIO, required = false) String usuario,
            @PathVariable Long id) {
        solicitudService.retirar(autorizacion.identificar(rol, usuario), id);
    }
}

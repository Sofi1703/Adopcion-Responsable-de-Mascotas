package co.edu.autonoma.adopcionmascotas.controller;

import co.edu.autonoma.adopcionmascotas.dto.MascotaRequest;
import co.edu.autonoma.adopcionmascotas.dto.MascotaResponse;
import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.service.AutorizacionService;
import co.edu.autonoma.adopcionmascotas.service.MascotaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;
    private final AutorizacionService autorizacion;

    // catalogo publico (por ejemplo /api/mascotas?estado=DISPONIBLE)
    @GetMapping
    public List<MascotaResponse> listar(@RequestParam(required = false) EstadoMascota estado) {
        return mascotaService.listar(estado);
    }

    @GetMapping("/{id}")
    public MascotaResponse obtener(@PathVariable Long id) {
        return mascotaService.obtener(id);
    }

    // las cabeceras no son obligatorias para Spring (el 401 lo da AutorizacionService con el JSON de error)
    @PostMapping
    public ResponseEntity<MascotaResponse> crear(
            @RequestHeader(name = AutorizacionService.CABECERA_ROL, required = false) String rol,
            @RequestHeader(name = AutorizacionService.CABECERA_USUARIO, required = false) String usuario,
            @Valid @RequestBody MascotaRequest request) {
        MascotaResponse creada = mascotaService.crear(autorizacion.identificar(rol, usuario), request);
        return ResponseEntity.created(URI.create("/api/mascotas/" + creada.getId())).body(creada);
    }

    @PutMapping("/{id}")
    public MascotaResponse reemplazar(
            @RequestHeader(name = AutorizacionService.CABECERA_ROL, required = false) String rol,
            @RequestHeader(name = AutorizacionService.CABECERA_USUARIO, required = false) String usuario,
            @PathVariable Long id,
            @Valid @RequestBody MascotaRequest request) {
        return mascotaService.reemplazar(autorizacion.identificar(rol, usuario), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @RequestHeader(name = AutorizacionService.CABECERA_ROL, required = false) String rol,
            @RequestHeader(name = AutorizacionService.CABECERA_USUARIO, required = false) String usuario,
            @PathVariable Long id) {
        mascotaService.eliminar(autorizacion.identificar(rol, usuario), id);
    }
}

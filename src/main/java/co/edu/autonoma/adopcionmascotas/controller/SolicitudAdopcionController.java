package co.edu.autonoma.adopcionmascotas.controller;

import co.edu.autonoma.adopcionmascotas.dto.SolicitudDetalleResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudRequest;
import co.edu.autonoma.adopcionmascotas.service.SolicitudService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// la ruta de creacion es distinta a /api/solicitudes porque asi la define el enunciado
@RestController
@RequestMapping("/api/solicitudes-adopcion")
@RequiredArgsConstructor
public class SolicitudAdopcionController {

    private final SolicitudService solicitudService;

    // publico (no requiere cabeceras porque el solicitante no tiene cuenta)
    @PostMapping
    public ResponseEntity<SolicitudDetalleResponse> crear(@Valid @RequestBody SolicitudRequest request) {
        SolicitudDetalleResponse creada = solicitudService.crear(request);
        return ResponseEntity.created(URI.create("/api/solicitudes/" + creada.getId())).body(creada);
    }
}

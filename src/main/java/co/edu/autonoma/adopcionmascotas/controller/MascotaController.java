package co.edu.autonoma.adopcionmascotas.controller;

import co.edu.autonoma.adopcionmascotas.dto.MascotaResponse;
import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.service.MascotaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    // catalogo publico (ej. /api/mascotas?estado=DISPONIBLE)
    @GetMapping
    public List<MascotaResponse> listar(@RequestParam(required = false) EstadoMascota estado) {
        return mascotaService.listar(estado);
    }

    @GetMapping("/{id}")
    public MascotaResponse obtener(@PathVariable Long id) {
        return mascotaService.obtener(id);
    }
}

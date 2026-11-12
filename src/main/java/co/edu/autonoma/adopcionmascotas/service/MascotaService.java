package co.edu.autonoma.adopcionmascotas.service;

import co.edu.autonoma.adopcionmascotas.dto.MascotaRequest;
import co.edu.autonoma.adopcionmascotas.dto.MascotaResponse;
import co.edu.autonoma.adopcionmascotas.exception.ApiException;
import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import co.edu.autonoma.adopcionmascotas.repository.MascotaRepository;
import co.edu.autonoma.adopcionmascotas.repository.SolicitudRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final SolicitudRepository solicitudRepository;
    private final AutorizacionService autorizacion;

    // sin estado devuelve todas las mascotas
    public List<MascotaResponse> listar(EstadoMascota estado) {
        List<Mascota> mascotas = estado == null
                ? mascotaRepository.findAllByOrderByIdAsc()
                : mascotaRepository.findByEstadoOrderByIdAsc(estado);
        return mascotas.stream().map(MascotaResponse::desde).toList();
    }

    public MascotaResponse obtener(Long id) {
        return MascotaResponse.desde(buscar(id));
    }

    // la mascota nace DISPONIBLE (valor por defecto de la entidad)
    @Transactional
    public MascotaResponse crear(Identidad identidad, MascotaRequest request) {
        autorizacion.exigirFundacion(identidad);

        Mascota mascota = Mascota.builder()
                .nombre(request.getNombre().trim())
                .especie(request.getEspecie().trim())
                .raza(limpiar(request.getRaza()))
                .descripcion(limpiar(request.getDescripcion()))
                .imagenUrl(limpiar(request.getImagenUrl()))
                .build();
        return MascotaResponse.desde(mascotaRepository.save(mascota));
    }

    // reemplaza la ficha completa y los opcionales que no se envien quedan en null
    @Transactional
    public MascotaResponse reemplazar(Identidad identidad, Long id, MascotaRequest request) {
        autorizacion.exigirFundacion(identidad);

        Mascota mascota = buscar(id);
        mascota.setNombre(request.getNombre().trim());
        mascota.setEspecie(request.getEspecie().trim());
        mascota.setRaza(limpiar(request.getRaza()));
        mascota.setDescripcion(limpiar(request.getDescripcion()));
        mascota.setImagenUrl(limpiar(request.getImagenUrl()));
        return MascotaResponse.desde(mascota);
    }

    // no se elimina una mascota con solicitudes porque se perderia la trazabilidad
    @Transactional
    public void eliminar(Identidad identidad, Long id) {
        autorizacion.exigirFundacion(identidad);

        Mascota mascota = buscar(id);
        if (solicitudRepository.existsByMascotaId(id)) {
            throw ApiException.conflicto("CONFLICTO",
                    "La mascota tiene solicitudes registradas y no se puede eliminar");
        }
        mascotaRepository.delete(mascota);
    }

    private Mascota buscar(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la mascota " + id));
    }

    // un opcional vacio se guarda como null
    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

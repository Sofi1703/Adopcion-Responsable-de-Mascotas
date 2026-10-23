package co.edu.autonoma.adopcionmascotas.service;

import co.edu.autonoma.adopcionmascotas.dto.MascotaResponse;
import co.edu.autonoma.adopcionmascotas.exception.ApiException;
import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import co.edu.autonoma.adopcionmascotas.repository.MascotaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MascotaService {

    private final MascotaRepository mascotaRepository;

    public MascotaService(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    // sin estado devuelve todas las mascotas
    public List<MascotaResponse> listar(EstadoMascota estado) {
        List<Mascota> mascotas = estado == null
                ? mascotaRepository.findAllByOrderByIdAsc()
                : mascotaRepository.findByEstadoOrderByIdAsc(estado);
        return mascotas.stream().map(MascotaResponse::desde).toList();
    }

    public MascotaResponse obtener(Long id) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la mascota " + id));
        return MascotaResponse.desde(mascota);
    }
}

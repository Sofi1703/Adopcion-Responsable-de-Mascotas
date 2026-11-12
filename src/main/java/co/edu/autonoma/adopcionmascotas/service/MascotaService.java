package co.edu.autonoma.adopcionmascotas.service;

import co.edu.autonoma.adopcionmascotas.dto.MascotaResponse;
import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import co.edu.autonoma.adopcionmascotas.repository.MascotaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MascotaService {

    private final MascotaRepository mascotaRepository;

    // sin estado devuelve todas las mascotas
    public List<MascotaResponse> listar(EstadoMascota estado) {
        List<Mascota> mascotas = estado == null
                ? mascotaRepository.findAllByOrderByIdAsc()
                : mascotaRepository.findByEstadoOrderByIdAsc(estado);
        return mascotas.stream().map(MascotaResponse::desde).toList();
    }
}

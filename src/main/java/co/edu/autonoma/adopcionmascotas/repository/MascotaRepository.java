package co.edu.autonoma.adopcionmascotas.repository;

import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findAllByOrderByIdAsc();

    List<Mascota> findByEstadoOrderByIdAsc(EstadoMascota estado);
}

package co.edu.autonoma.adopcionmascotas.repository;

import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findAllByOrderByIdAsc();

    List<Mascota> findByEstadoOrderByIdAsc(EstadoMascota estado);

    // bloquea la fila (SELECT FOR UPDATE) para que dos peticiones sobre la misma mascota se atiendan una tras otra
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mascota m where m.id = :id")
    Optional<Mascota> buscarConBloqueo(@Param("id") Long id);
}

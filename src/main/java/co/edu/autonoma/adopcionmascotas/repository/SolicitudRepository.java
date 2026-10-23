package co.edu.autonoma.adopcionmascotas.repository;

import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.Solicitud;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

// el EntityGraph trae la mascota en la misma consulta para armar los DTO sin consultas extra
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    @Override
    @EntityGraph(attributePaths = "mascota")
    Optional<Solicitud> findById(Long id);

    @EntityGraph(attributePaths = "mascota")
    List<Solicitud> findAllByOrderByCreadaEnDesc();

    @EntityGraph(attributePaths = "mascota")
    List<Solicitud> findByMascotaIdOrderByCreadaEnDesc(Long mascotaId);

    @EntityGraph(attributePaths = "mascota")
    List<Solicitud> findByEstadoOrderByCreadaEnDesc(EstadoSolicitud estado);

    @EntityGraph(attributePaths = "mascota")
    List<Solicitud> findByMascotaIdAndEstadoOrderByCreadaEnDesc(Long mascotaId, EstadoSolicitud estado);
}

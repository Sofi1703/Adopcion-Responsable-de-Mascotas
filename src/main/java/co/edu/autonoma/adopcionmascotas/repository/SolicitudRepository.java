package co.edu.autonoma.adopcionmascotas.repository;

import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.Solicitud;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // RN-01 mismo criterio que el indice uq_solicitud_activa de la base
    @Query("""
            select count(s) > 0 from Solicitud s
            where s.mascota.id = :mascotaId
              and lower(s.solicitanteCorreo) = lower(:correo)
              and s.estado in :estados
              and s.retiradaEn is null
            """)
    boolean existeActiva(@Param("mascotaId") Long mascotaId, @Param("correo") String correo,
            @Param("estados") Collection<EstadoSolicitud> estados);

    // RN-04 las demas solicitudes activas de la mascota
    @Query("""
            select s from Solicitud s
            where s.mascota.id = :mascotaId
              and s.id <> :excluirId
              and s.estado in :estados
              and s.retiradaEn is null
            """)
    List<Solicitud> buscarActivasDeMascota(@Param("mascotaId") Long mascotaId, @Param("excluirId") Long excluirId,
            @Param("estados") Collection<EstadoSolicitud> estados);

    // trae solo el id de la mascota sin cargar entidades (para bloquear la mascota antes de leer la solicitud)
    @Query("select s.mascota.id from Solicitud s where s.id = :id")
    Optional<Long> buscarMascotaId(@Param("id") Long id);
}

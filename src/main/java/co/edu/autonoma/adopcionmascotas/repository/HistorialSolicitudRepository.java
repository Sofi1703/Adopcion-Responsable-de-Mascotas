package co.edu.autonoma.adopcionmascotas.repository;

import co.edu.autonoma.adopcionmascotas.model.HistorialSolicitud;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialSolicitudRepository extends JpaRepository<HistorialSolicitud, Long> {

    List<HistorialSolicitud> findBySolicitudIdOrderByFechaAscIdAsc(Long solicitudId);
}

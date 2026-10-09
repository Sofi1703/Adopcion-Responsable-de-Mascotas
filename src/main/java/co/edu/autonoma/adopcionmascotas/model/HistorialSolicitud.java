package co.edu.autonoma.adopcionmascotas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

// para la trazabilidad solo se insertan filas y nunca se modifican
@Entity
@Immutable
@Table(name = "historial_solicitud")
public class HistorialSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false, updatable = false)
    private Solicitud solicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoHistorial tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoSolicitud estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", length = 20)
    private EstadoSolicitud estadoNuevo;

    // quien hizo el cambio (SISTEMA en los cierres automaticos de RN-04)
    @Column(nullable = false, length = 150)
    private String actor;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected HistorialSolicitud() {
    }

    public HistorialSolicitud(Solicitud solicitud, TipoHistorial tipo,
            EstadoSolicitud estadoAnterior, EstadoSolicitud estadoNuevo, String actor) {
        this.solicitud = solicitud;
        this.tipo = tipo;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.actor = actor;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public TipoHistorial getTipo() {
        return tipo;
    }

    public EstadoSolicitud getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoSolicitud getEstadoNuevo() {
        return estadoNuevo;
    }

    public String getActor() {
        return actor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

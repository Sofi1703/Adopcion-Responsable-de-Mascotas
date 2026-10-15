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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mascota_id", nullable = false, updatable = false)
    private Mascota mascota;

    @Column(name = "solicitante_correo", nullable = false, length = 150)
    private String solicitanteCorreo;

    @Column(name = "solicitante_nombre", nullable = false, length = 100)
    private String solicitanteNombre;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.RECIBIDA;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn;

    // retirar no borra la fila y solo marca la fecha para conservar la trazabilidad
    @Column(name = "retirada_en")
    private LocalDateTime retiradaEn;

    protected Solicitud() {
    }

    public Solicitud(Mascota mascota, String solicitanteCorreo, String solicitanteNombre, String telefono) {
        this.mascota = mascota;
        this.solicitanteCorreo = solicitanteCorreo;
        this.solicitanteNombre = solicitanteNombre;
        this.telefono = telefono;
    }

    @PrePersist
    void antesDeGuardar() {
        if (creadaEn == null) {
            creadaEn = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public String getSolicitanteCorreo() {
        return solicitanteCorreo;
    }

    public String getSolicitanteNombre() {
        return solicitanteNombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public LocalDateTime getRetiradaEn() {
        return retiradaEn;
    }

    public void setRetiradaEn(LocalDateTime retiradaEn) {
        this.retiradaEn = retiradaEn;
    }
}

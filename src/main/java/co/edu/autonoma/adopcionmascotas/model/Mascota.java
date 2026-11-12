package co.edu.autonoma.adopcionmascotas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// sin @Data porque equals y hashCode generados no son seguros en entidades JPA
@Entity
@Table(name = "mascota")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String especie;

    @Column(length = 100)
    private String raza;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMascota estado = EstadoMascota.DISPONIBLE;

    // la imagen es una URL opcional que se guarda como texto (RN-06)
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    // la mascota siempre nace DISPONIBLE y por eso el estado no va en el builder
    @Builder
    public Mascota(String nombre, String especie, String raza, String descripcion, String imagenUrl) {
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }
}

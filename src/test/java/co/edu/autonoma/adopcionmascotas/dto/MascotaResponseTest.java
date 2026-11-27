package co.edu.autonoma.adopcionmascotas.dto;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MascotaResponseTest {

    @Test
    void desde_mascotaNueva_copiaLosDatosYQuedaDisponible() {
        // arrange (el id lo genera la base y aqui se pone a mano)
        Mascota mascota = Mascota.builder()
                .nombre("Luna")
                .especie("Perro")
                .raza("Criolla")
                .descripcion("Juguetona")
                .imagenUrl("https://ejemplo.com/luna.jpg")
                .build();
        ReflectionTestUtils.setField(mascota, "id", 1L);

        // act
        MascotaResponse respuesta = MascotaResponse.desde(mascota);

        // assert
        assertThat(respuesta.getId()).isEqualTo(1L);
        assertThat(respuesta.getNombre()).isEqualTo("Luna");
        assertThat(respuesta.getEspecie()).isEqualTo("Perro");
        assertThat(respuesta.getRaza()).isEqualTo("Criolla");
        assertThat(respuesta.getImagenUrl()).isEqualTo("https://ejemplo.com/luna.jpg");
        assertThat(respuesta.getEstado()).isEqualTo(EstadoMascota.DISPONIBLE);
    }
}

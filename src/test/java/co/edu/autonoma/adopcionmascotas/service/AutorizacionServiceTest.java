package co.edu.autonoma.adopcionmascotas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.autonoma.adopcionmascotas.exception.ApiException;
import co.edu.autonoma.adopcionmascotas.model.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

// no tiene dependencias y por eso no necesita mocks
class AutorizacionServiceTest {

    private final AutorizacionService autorizacion = new AutorizacionService();

    @Test
    void identificar_rolEnMinusculas_reconoceElRol() {
        Identidad identidad = autorizacion.identificar("coordinador", " laura ");

        assertThat(identidad.getRol()).isEqualTo(Rol.COORDINADOR);
        assertThat(identidad.getUsuario()).isEqualTo("laura");
        assertThat(identidad.esPersonal()).isTrue();
    }

    @Test
    void identificar_sinCabeceras_lanza401() {
        ApiException error = assertThrows(ApiException.class, () -> autorizacion.identificar(null, null));

        assertThat(error.getEstado()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(error.getCodigo()).isEqualTo("NO_AUTENTICADO");
    }

    @Test
    void identificar_rolInexistente_lanza401() {
        ApiException error = assertThrows(ApiException.class, () -> autorizacion.identificar("ADMIN", "laura"));

        assertThat(error.getEstado()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}

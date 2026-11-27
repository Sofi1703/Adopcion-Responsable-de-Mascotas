package co.edu.autonoma.adopcionmascotas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.autonoma.adopcionmascotas.dto.MascotaRequest;
import co.edu.autonoma.adopcionmascotas.dto.MascotaResponse;
import co.edu.autonoma.adopcionmascotas.exception.ApiException;
import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import co.edu.autonoma.adopcionmascotas.model.Rol;
import co.edu.autonoma.adopcionmascotas.repository.MascotaRepository;
import co.edu.autonoma.adopcionmascotas.repository.SolicitudRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MascotaServiceTest {

    private static final Identidad FUNDACION = new Identidad(Rol.FUNDACION, "fundacion");
    private static final Identidad SOLICITANTE = new Identidad(Rol.SOLICITANTE, "ana@correo.com");

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    // la autorizacion real (no un mock) para que las reglas de rol se ejecuten de verdad
    @Spy
    private AutorizacionService autorizacion = new AutorizacionService();

    @InjectMocks
    private MascotaService service;

    @Test
    void obtener_existente_devuelveLaMascota() {
        // arrange
        Mascota mascota = mascota(1L, "Luna");
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascota));

        // act
        MascotaResponse respuesta = service.obtener(1L);

        // assert
        assertThat(respuesta.getId()).isEqualTo(1L);
        assertThat(respuesta.getNombre()).isEqualTo("Luna");
    }

    @Test
    void obtener_inexistente_lanza404() {
        // arrange
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        // act
        ApiException error = assertThrows(ApiException.class, () -> service.obtener(99L));

        // assert
        assertThat(error.getEstado()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(error.getMessage()).contains("99");
    }

    @Test
    void crear_comoFundacion_quedaDisponibleYLimpiaLosOpcionales() {
        // arrange (save devuelve el mismo objeto que recibe como haria la base)
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        MascotaRequest request = new MascotaRequest("  Nala ", "Gato", "   ", "", "https://ejemplo.com/nala.jpg");

        // act
        MascotaResponse respuesta = service.crear(FUNDACION, request);

        // assert
        assertThat(respuesta.getEstado()).isEqualTo(EstadoMascota.DISPONIBLE);
        assertThat(respuesta.getNombre()).isEqualTo("Nala");
        assertThat(respuesta.getRaza()).isNull();
        assertThat(respuesta.getDescripcion()).isNull();
        verify(mascotaRepository).save(any(Mascota.class));
    }

    @Test
    void crear_comoSolicitante_lanza403YNoGuarda() {
        MascotaRequest request = new MascotaRequest("Toby", "Perro", null, null, null);

        ApiException error = assertThrows(ApiException.class, () -> service.crear(SOLICITANTE, request));

        assertThat(error.getEstado()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(mascotaRepository);
    }

    @Test
    void eliminar_sinSolicitudes_borraLaMascotaEncontrada() {
        Mascota mascota = mascota(1L, "Temporal");
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascota));
        when(solicitudRepository.existsByMascotaId(1L)).thenReturn(false);

        service.eliminar(FUNDACION, 1L);

        verify(mascotaRepository).delete(mascota);
    }

    @Test
    void eliminar_conSolicitudes_lanza409YNoBorra() {
        Mascota mascota = mascota(1L, "Luna");
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascota));
        when(solicitudRepository.existsByMascotaId(1L)).thenReturn(true);

        ApiException error = assertThrows(ApiException.class, () -> service.eliminar(FUNDACION, 1L));

        assertThat(error.getEstado()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(error.getCodigo()).isEqualTo("CONFLICTO");
        verify(mascotaRepository, never()).delete(any());
    }

    // el id lo genera la base y en una prueba unitaria se asigna a mano
    private static Mascota mascota(Long id, String nombre) {
        Mascota mascota = Mascota.builder().nombre(nombre).especie("Perro").build();
        ReflectionTestUtils.setField(mascota, "id", id);
        return mascota;
    }
}

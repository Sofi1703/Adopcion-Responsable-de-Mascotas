package co.edu.autonoma.adopcionmascotas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.autonoma.adopcionmascotas.dto.CambioEstadoRequest;
import co.edu.autonoma.adopcionmascotas.dto.CambioEstadoResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudDetalleResponse;
import co.edu.autonoma.adopcionmascotas.dto.SolicitudRequest;
import co.edu.autonoma.adopcionmascotas.exception.ApiException;
import co.edu.autonoma.adopcionmascotas.model.EstadoMascota;
import co.edu.autonoma.adopcionmascotas.model.EstadoSolicitud;
import co.edu.autonoma.adopcionmascotas.model.HistorialSolicitud;
import co.edu.autonoma.adopcionmascotas.model.Mascota;
import co.edu.autonoma.adopcionmascotas.model.Rol;
import co.edu.autonoma.adopcionmascotas.model.Solicitud;
import co.edu.autonoma.adopcionmascotas.model.TipoHistorial;
import co.edu.autonoma.adopcionmascotas.repository.HistorialSolicitudRepository;
import co.edu.autonoma.adopcionmascotas.repository.MascotaRepository;
import co.edu.autonoma.adopcionmascotas.repository.SolicitudRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    private static final Identidad COORDINADOR = new Identidad(Rol.COORDINADOR, "laura");
    private static final Identidad ANA = new Identidad(Rol.SOLICITANTE, "ana@correo.com");
    private static final Identidad CARLOS = new Identidad(Rol.SOLICITANTE, "carlos@correo.com");

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private HistorialSolicitudRepository historialRepository;

    // la autorizacion real (no un mock) para que las reglas de rol se ejecuten de verdad
    @Spy
    private AutorizacionService autorizacion = new AutorizacionService();

    @InjectMocks
    private SolicitudService service;

    // crear (POST /api/solicitudes-adopcion)

    @Test
    void crear_mascotaDisponible_quedaRecibidaYRegistraLaCreacion() {
        // arrange
        Mascota luna = mascota(1L, EstadoMascota.DISPONIBLE);
        when(mascotaRepository.buscarConBloqueo(1L)).thenReturn(Optional.of(luna));
        when(solicitudRepository.existeActiva(eq(1L), eq("ana@correo.com"), anyCollection())).thenReturn(false);
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        SolicitudRequest request = new SolicitudRequest(1L, "Ana Perez", "  ana@correo.com ", "3103334455");

        // act
        SolicitudDetalleResponse respuesta = service.crear(request);

        // assert
        assertThat(respuesta.getEstado()).isEqualTo(EstadoSolicitud.RECIBIDA);
        assertThat(respuesta.getSolicitanteCorreo()).isEqualTo("ana@correo.com");
        assertThat(respuesta.getMascotaNombre()).isEqualTo("Luna");

        ArgumentCaptor<HistorialSolicitud> evento = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(evento.capture());
        assertThat(evento.getValue().getTipo()).isEqualTo(TipoHistorial.CREACION);
        assertThat(evento.getValue().getEstadoNuevo()).isEqualTo(EstadoSolicitud.RECIBIDA);
        assertThat(evento.getValue().getActor()).isEqualTo("ana@correo.com");
    }

    @Test
    void crear_mascotaAdoptada_lanzaRN05YNoGuarda() {
        when(mascotaRepository.buscarConBloqueo(1L)).thenReturn(Optional.of(mascota(1L, EstadoMascota.ADOPTADA)));
        SolicitudRequest request = new SolicitudRequest(1L, "Ana Perez", "ana@correo.com", "3103334455");

        ApiException error = assertThrows(ApiException.class, () -> service.crear(request));

        assertThat(error.getEstado()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(error.getCodigo()).isEqualTo("RN-05");
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void crear_solicitudActivaRepetida_lanzaRN01YNoGuarda() {
        when(mascotaRepository.buscarConBloqueo(1L)).thenReturn(Optional.of(mascota(1L, EstadoMascota.DISPONIBLE)));
        when(solicitudRepository.existeActiva(eq(1L), eq("ana@correo.com"), anyCollection())).thenReturn(true);
        SolicitudRequest request = new SolicitudRequest(1L, "Ana Perez", "ana@correo.com", "3103334455");

        ApiException error = assertThrows(ApiException.class, () -> service.crear(request));

        assertThat(error.getCodigo()).isEqualTo("RN-01");
        verify(solicitudRepository, never()).save(any());
        verifyNoInteractions(historialRepository);
    }

    @Test
    void crear_mascotaInexistente_lanza404() {
        when(mascotaRepository.buscarConBloqueo(99L)).thenReturn(Optional.empty());
        SolicitudRequest request = new SolicitudRequest(99L, "Ana Perez", "ana@correo.com", "3103334455");

        ApiException error = assertThrows(ApiException.class, () -> service.crear(request));

        assertThat(error.getEstado()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // obtener (GET /api/solicitudes/{id})

    @Test
    void obtener_solicitanteAjeno_lanza403() {
        Solicitud deAna = solicitud(10L, mascota(1L, EstadoMascota.DISPONIBLE), "ana@correo.com", EstadoSolicitud.RECIBIDA);
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(deAna));

        ApiException error = assertThrows(ApiException.class, () -> service.obtener(CARLOS, 10L));

        assertThat(error.getEstado()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // cambiarEstado (PATCH /api/solicitudes/{id}/estado)

    @Test
    void cambiarEstado_aprobar_adoptaLaMascotaYRechazaLasDemas() {
        // arrange
        Mascota luna = mascota(1L, EstadoMascota.DISPONIBLE);
        Solicitud deAna = solicitud(10L, luna, "ana@correo.com", EstadoSolicitud.EN_REVISION);
        Solicitud deCarlos = solicitud(11L, luna, "carlos@correo.com", EstadoSolicitud.RECIBIDA);
        when(solicitudRepository.buscarMascotaId(10L)).thenReturn(Optional.of(1L));
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(deAna));
        when(solicitudRepository.buscarActivasDeMascota(eq(1L), eq(10L), anyCollection())).thenReturn(List.of(deCarlos));

        // act
        CambioEstadoResponse respuesta = service.cambiarEstado(COORDINADOR, 10L,
                new CambioEstadoRequest(EstadoSolicitud.APROBADA));

        // assert RN-03
        assertThat(respuesta.getEstado()).isEqualTo(EstadoSolicitud.APROBADA);
        assertThat(respuesta.getMascotaEstado()).isEqualTo(EstadoMascota.ADOPTADA);
        assertThat(luna.getEstado()).isEqualTo(EstadoMascota.ADOPTADA);

        // assert RN-04
        assertThat(respuesta.getSolicitudesCerradas()).containsExactly(11L);
        assertThat(deCarlos.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADA);

        ArgumentCaptor<HistorialSolicitud> eventos = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository, times(2)).save(eventos.capture());
        assertThat(eventos.getAllValues())
                .extracting(HistorialSolicitud::getTipo, HistorialSolicitud::getActor)
                .containsExactly(
                        tuple(TipoHistorial.CAMBIO_ESTADO, "laura"),
                        tuple(TipoHistorial.CIERRE_AUTOMATICO, "SISTEMA"));
    }

    @Test
    void cambiarEstado_deRecibidaAAprobada_lanzaRN02YNoCambiaNada() {
        Solicitud deAna = solicitud(10L, mascota(1L, EstadoMascota.DISPONIBLE), "ana@correo.com", EstadoSolicitud.RECIBIDA);
        when(solicitudRepository.buscarMascotaId(10L)).thenReturn(Optional.of(1L));
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(deAna));

        ApiException error = assertThrows(ApiException.class, () -> service.cambiarEstado(COORDINADOR, 10L,
                new CambioEstadoRequest(EstadoSolicitud.APROBADA)));

        assertThat(error.getCodigo()).isEqualTo("RN-02");
        assertThat(deAna.getEstado()).isEqualTo(EstadoSolicitud.RECIBIDA);
        verify(historialRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_comoSolicitante_lanza403SinTocarLaBase() {
        ApiException error = assertThrows(ApiException.class, () -> service.cambiarEstado(ANA, 10L,
                new CambioEstadoRequest(EstadoSolicitud.EN_REVISION)));

        assertThat(error.getEstado()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(solicitudRepository, mascotaRepository, historialRepository);
    }

    // retirar (DELETE /api/solicitudes/{id})

    @Test
    void retirar_propiaRecibida_marcaRetiradaYRegistraElRetiro() {
        Solicitud deAna = solicitud(10L, mascota(1L, EstadoMascota.DISPONIBLE), "ana@correo.com", EstadoSolicitud.RECIBIDA);
        when(solicitudRepository.buscarMascotaId(10L)).thenReturn(Optional.of(1L));
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(deAna));

        // el correo de la cabecera llega en mayusculas y debe reconocerse igual
        service.retirar(new Identidad(Rol.SOLICITANTE, "ANA@correo.com"), 10L);

        assertThat(deAna.getRetiradaEn()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
        assertThat(deAna.getEstado()).isEqualTo(EstadoSolicitud.RECIBIDA);
        ArgumentCaptor<HistorialSolicitud> evento = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(evento.capture());
        assertThat(evento.getValue().getTipo()).isEqualTo(TipoHistorial.RETIRO);
    }

    @Test
    void retirar_enRevision_lanza409YNoLaRetira() {
        Solicitud deAna = solicitud(10L, mascota(1L, EstadoMascota.DISPONIBLE), "ana@correo.com", EstadoSolicitud.EN_REVISION);
        when(solicitudRepository.buscarMascotaId(10L)).thenReturn(Optional.of(1L));
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(deAna));

        ApiException error = assertThrows(ApiException.class, () -> service.retirar(ANA, 10L));

        assertThat(error.getEstado()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(deAna.getRetiradaEn()).isNull();
        verify(historialRepository, never()).save(any());
    }

    // los ids los genera la base y en una prueba unitaria se asignan a mano

    private static Mascota mascota(Long id, EstadoMascota estado) {
        Mascota mascota = Mascota.builder().nombre("Luna").especie("Perro").build();
        mascota.setEstado(estado);
        ReflectionTestUtils.setField(mascota, "id", id);
        return mascota;
    }

    private static Solicitud solicitud(Long id, Mascota mascota, String correo, EstadoSolicitud estado) {
        Solicitud solicitud = Solicitud.builder()
                .mascota(mascota)
                .solicitanteCorreo(correo)
                .solicitanteNombre("Solicitante")
                .telefono("3000000000")
                .build();
        solicitud.setEstado(estado);
        ReflectionTestUtils.setField(solicitud, "id", id);
        return solicitud;
    }
}

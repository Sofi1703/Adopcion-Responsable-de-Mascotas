package co.edu.autonoma.adopcionmascotas.service;

import co.edu.autonoma.adopcionmascotas.exception.ApiException;
import co.edu.autonoma.adopcionmascotas.model.Rol;
import java.util.Locale;
import org.springframework.stereotype.Service;

// autenticacion simulada por cabeceras (no hay login real)
@Service
public class AutorizacionService {

    public static final String CABECERA_ROL = "X-Rol";
    public static final String CABECERA_USUARIO = "X-Usuario";

    public Identidad identificar(String rol, String usuario) {
        if (rol == null || rol.isBlank() || usuario == null || usuario.isBlank()) {
            throw ApiException.noAutenticado("Faltan las cabeceras " + CABECERA_ROL + " y " + CABECERA_USUARIO);
        }
        try {
            return new Identidad(Rol.valueOf(rol.trim().toUpperCase(Locale.ROOT)), usuario.trim());
        } catch (IllegalArgumentException e) {
            throw ApiException.noAutenticado(CABECERA_ROL + " debe ser FUNDACION, COORDINADOR o SOLICITANTE");
        }
    }

    public void exigirPersonal(Identidad identidad) {
        if (!identidad.esPersonal()) {
            throw ApiException.sinPermiso("Esta consulta es solo para la fundacion y los coordinadores");
        }
    }
}

package co.edu.autonoma.adopcionmascotas.service;

import co.edu.autonoma.adopcionmascotas.model.Rol;

// quien hace la peticion (sale de las cabeceras X-Rol y X-Usuario)
public record Identidad(Rol rol, String usuario) {

    // fundacion y coordinador son el personal que gestiona las solicitudes
    public boolean esPersonal() {
        return rol == Rol.FUNDACION || rol == Rol.COORDINADOR;
    }
}

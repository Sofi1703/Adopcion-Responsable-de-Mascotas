package co.edu.autonoma.adopcionmascotas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// un campo que no paso la validacion
@Getter
@AllArgsConstructor
public class ErrorCampo {

    private String campo;
    private String mensaje;
}

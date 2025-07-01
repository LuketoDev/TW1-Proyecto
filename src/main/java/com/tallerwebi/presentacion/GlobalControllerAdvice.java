package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("datosLogin")
    public DatosLoginDto datosLoginDto() {
        return new DatosLoginDto();
    }


}
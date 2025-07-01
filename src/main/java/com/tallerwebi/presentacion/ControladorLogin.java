package com.tallerwebi.presentacion;

import com.tallerwebi.core.FragmentRenderer;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;

    private FragmentRenderer fragmentRenderer;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin, FragmentRenderer fragmentRenderer) {
        this.servicioLogin = servicioLogin;
        this.fragmentRenderer = fragmentRenderer;
    }


//    @RequestMapping(path = "/nuevo-login", method = RequestMethod.GET)
//    public ModelAndView nuevoLogin() {
//        ModelMap model = new ModelMap();
//        model.put("datosLogin", new DatosLoginDto()); // 👈 esto es lo importante
//        return new ModelAndView("fragments/login", model);
//    }

    //    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
//    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLoginDto datosLogin, HttpServletRequest request) {
//        ModelMap model = new ModelMap();
//
//        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
//        if (usuarioBuscado != null) {
//            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
//            return new ModelAndView("redirect:/home");
//        } else {
//            model.put("error", "Usuario o clave incorrecta");
//        }
//        return new ModelAndView("login", model);
//    }
    @RequestMapping("/login")
    public ModelAndView irALogin() {
        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLoginDto());
        return new ModelAndView("fragments/login", modelo);
    }
//
//    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
//    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLoginDto datosLogin,
////                                     @RequestParam("redirectUrl") String redirectUrl,
//                                     HttpServletRequest request) {
//        ModelMap model = new ModelMap();
//
////        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
//        Usuario usuarioBuscado = servicioLogin.obtenerUsuarioPorEmailYPassword(datosLogin.getEmail(), datosLogin.getPassword());
//        if (usuarioBuscado != null) {
//            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
//
////            if (redirectUrl != null && redirectUrl.startsWith("/")) {
////                return new ModelAndView("redirect:" + redirectUrl);
////            }
//            model.put("mensajeLoginExitoso", "El Login fue exitoso");
//
//            return new ModelAndView("redirect:/index", model);
//        } else {
//            model.put("error", "Usuario o clave incorrecta");
//            return new ModelAndView("fragments/login", model);
//        }
//    }

@PostMapping("/validar-login")
public ResponseEntity<String> validarLogin(@ModelAttribute("datosLogin") DatosLoginDto datosLogin,
//                                           @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           Model model) {

    Usuario usuario = servicioLogin.obtenerUsuarioPorEmailYPassword(datosLogin.getEmail(), datosLogin.getPassword());

    if (usuario != null) {
        request.getSession().setAttribute("ROL", usuario.getRol());
        // Le decimos al JS que redireccione
        return ResponseEntity.status(202).body("REDIRECT:/index");
    }

    model.addAttribute("error", "Usuario o clave incorrecta");
    model.addAttribute("datosLogin", datosLogin);

    // Renderizamos el HTML del fragmento con el error
    String html = fragmentRenderer.render("fragments/login :: login", model, request, response);
    return ResponseEntity.status(401).body(html);
}

    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario, @RequestParam("redirectUrl") String redirectUrl) {
        ModelMap model = new ModelMap();
        try {
            servicioLogin.registrar(usuario);
        } catch (UsuarioExistente e) {
            model.put("error", "El usuario ya existe");
            return new ModelAndView("fragments/nuevo-usuario", model);
        } catch (Exception e) {
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("fragments/nuevo-usuario", model);
        }
        model.put("mensajeRegistroExitoso", "El Registro fue exitoso");

        return new ModelAndView("redirect:/index", model);
    }

    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario());
        return new ModelAndView("fragments/nuevo-usuario", model);
    }

    @RequestMapping(path = "/home", method = RequestMethod.GET)
    public ModelAndView irAHome() {
        return new ModelAndView("home");
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ModelAndView inicio() {
        return new ModelAndView("redirect:/index");
    }
}
package com.vidasalud.bff.controller;

import com.vidasalud.bff.security.IdentidadUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private static final String PREFIJO_ROL = "ROLE_";

    private final IdentidadUsuario identidad;

    public MeController(IdentidadUsuario identidad) {
        this.identidad = identidad;
    }

    /**
     * Datos del usuario autenticado. nombre es el nombre para mostrar (saludo); nombreCompleto, nombre y apellido
     * (ficha clínica). Los roles son los efectivos (incluye Paciente por defecto para cuentas
     * recién registradas). Los usuarios externos no siempre traen todos los claims, por eso se usan alternativas.
     */
    @GetMapping
    public Map<String, Object> me(Authentication autenticacion) {
        Jwt jwt = (Jwt) autenticacion.getPrincipal();
        String usuario = primeroNoVacio(identidad.correo(autenticacion), jwt.getSubject());
        String nombre = primeroNoVacio(jwt.getClaimAsString("name"), usuario);
        List<String> roles = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridad -> autoridad.startsWith(PREFIJO_ROL))
                .map(autoridad -> autoridad.substring(PREFIJO_ROL.length()))
                .toList();

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("nombre", nombre);
        respuesta.put("nombreCompleto", identidad.nombreCompleto(autenticacion));
        respuesta.put("usuario", usuario);
        respuesta.put("roles", roles);
        return respuesta;
    }

    private static String primeroNoVacio(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return "";
    }
}

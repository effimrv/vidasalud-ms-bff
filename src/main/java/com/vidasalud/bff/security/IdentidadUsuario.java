package com.vidasalud.bff.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Datos del usuario autenticado que el BFF usa para limitar lo que cada paciente puede ver. */
@Component
public class IdentidadUsuario {

    /** Roles del personal del centro, que pueden ver y gestionar las atenciones de todos los pacientes. */
    private static final Set<String> ROLES_PERSONAL = Set.of("ROLE_Admin", "ROLE_Recepcionista");

    public boolean esPersonal(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLES_PERSONAL::contains);
    }

    /**
     * Nombre formal para la ficha clínica: nombre y apellido (claims given_name y family_name, que vienen del
     * registro). Si no están, usa el nombre para mostrar y, en último caso, el usuario.
     */
    public String nombreCompleto(Authentication autenticacion) {
        Jwt jwt = (Jwt) autenticacion.getPrincipal();
        String completo = (texto(jwt.getClaimAsString("given_name")) + " " + texto(jwt.getClaimAsString("family_name"))).trim();
        if (!completo.isEmpty()) {
            return completo;
        }
        String nombre = texto(jwt.getClaimAsString("name"));
        return nombre.isEmpty() ? texto(jwt.getClaimAsString("preferred_username")) : nombre;
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    /** Identificador estable del usuario en Entra ID: claim oid (o sub si no viene). */
    public String idPaciente(Authentication autenticacion) {
        Jwt jwt = (Jwt) autenticacion.getPrincipal();
        String oid = jwt.getClaimAsString("oid");
        return (oid != null && !oid.isBlank()) ? oid : jwt.getSubject();
    }
}

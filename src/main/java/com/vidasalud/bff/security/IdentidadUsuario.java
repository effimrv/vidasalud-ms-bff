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

    /** Identificador estable del usuario en Entra ID: claim oid (o sub si no viene). */
    public String idPaciente(Authentication autenticacion) {
        Jwt jwt = (Jwt) autenticacion.getPrincipal();
        String oid = jwt.getClaimAsString("oid");
        return (oid != null && !oid.isBlank()) ? oid : jwt.getSubject();
    }
}

package pe.marathon.inventario.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Spring Security: BCrypt, Usuarios y Roles RBAC")
public class SecurityIntegrationTest {

    private final SecurityConfig securityConfig = new SecurityConfig();
    private final PasswordEncoder encoder = securityConfig.passwordEncoder();
    private final UserDetailsService userDetailsService = securityConfig.userDetailsService(encoder);

    @Test
    @DisplayName("BCrypt: Debe encriptar contraseñas con sal y validar coincidencia")
    void testBCryptEncoder() {
        String rawPassword = "auditor123";
        String encoded = encoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$"), "El hash debe ser formato BCrypt");
        assertTrue(encoder.matches(rawPassword, encoded), "La contraseña original debe coincidir con el hash");
        assertFalse(encoder.matches("wrongpassword", encoded), "Una contraseña incorrecta debe ser rechazada");
    }

    @Test
    @DisplayName("UserDetailsService: Debe registrar los 3 roles requeridos (OPERARIO, AUDITOR, SUPERVISOR)")
    void testUsuariosYRoles() {
        // 1. Operario
        UserDetails operario = userDetailsService.loadUserByUsername("operario");
        assertNotNull(operario);
        assertTrue(operario.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPERARIO")));

        // 2. Auditor
        UserDetails auditor = userDetailsService.loadUserByUsername("auditor");
        assertNotNull(auditor);
        assertTrue(auditor.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AUDITOR")));

        // 3. Supervisor
        UserDetails supervisor = userDetailsService.loadUserByUsername("supervisor");
        assertNotNull(supervisor);
        assertTrue(supervisor.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR")));
        assertTrue(supervisor.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}

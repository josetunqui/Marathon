package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.dto.LoginRequestDTO;
import pe.marathon.inventario.dto.LoginResponseDTO;
import pe.marathon.inventario.dto.UsuarioDemoDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador de Autenticación y Seguridad (Spring Security).
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Seguridad & Autenticación", description = "Autenticación basada en Spring Security, validación de credenciales y perfiles de usuario")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y validar credenciales con Spring Security")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String nombreCompleto = obtenerNombreCompleto(request.getUsername());
            String rolPrincipal = roles.stream()
                    .filter(r -> r.startsWith("ROLE_"))
                    .findFirst()
                    .map(r -> r.replace("ROLE_", ""))
                    .orElse("OPERARIO");

            return ResponseEntity.ok(LoginResponseDTO.builder()
                    .autenticado(true)
                    .mensaje("Autenticación exitosa con Spring Security")
                    .username(request.getUsername())
                    .nombreCompleto(nombreCompleto)
                    .rol(rolPrincipal)
                    .permisos(roles)
                    .tokenDemo("SEC-TOKEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .build());
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponseDTO.builder()
                    .autenticado(false)
                    .mensaje("Credenciales inválidas: Usuario o contraseña incorrectos")
                    .username(request.getUsername())
                    .build());
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Consultar el estado del módulo de seguridad y usuario activo")
    public ResponseEntity<Map<String, Object>> getSecurityStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("moduloSeguridad", "Spring Security 6");
        status.put("version", "3.2.4");
        status.put("algoritmoEncriptacion", "BCrypt (fuerza 10)");
        status.put("estado", "ACTIVO");

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            status.put("autenticado", true);
            status.put("usuario", auth.getName());
            status.put("roles", auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else {
            status.put("autenticado", false);
            status.put("usuario", "Invitado / Anónimo");
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/usuarios-demo")
    @Operation(summary = "Listar los usuarios de prueba configurados en Spring Security")
    public ResponseEntity<List<UsuarioDemoDTO>> listarUsuariosDemo() {
        List<UsuarioDemoDTO> usuarios = Arrays.asList(
                UsuarioDemoDTO.builder()
                        .username("operario")
                        .password("operario123")
                        .nombreCompleto("Jose Armando Tunqui (Operario)")
                        .rol("ROLE_OPERARIO")
                        .descripcion("Acceso al escáner de códigos de barra y consulta de stock en piso de venta")
                        .build(),
                UsuarioDemoDTO.builder()
                        .username("auditor")
                        .password("auditor123")
                        .nombreCompleto("Joseph Aaron Churampi (Auditor de Inventario)")
                        .rol("ROLE_AUDITOR")
                        .descripcion("Revisión de conciliación, discrepancias, exportación a Excel y Actas PDF")
                        .build(),
                UsuarioDemoDTO.builder()
                        .username("supervisor")
                        .password("admin123")
                        .nombreCompleto("Anita Condo López (Supervisora / Jefe de Tienda)")
                        .rol("ROLE_SUPERVISOR")
                        .descripcion("Aprobación definitiva de mermas y ajustes de inventario hacia SAP")
                        .build()
        );
        return ResponseEntity.ok(usuarios);
    }

    private String obtenerNombreCompleto(String username) {
        if ("operario".equalsIgnoreCase(username) || "jtunqui".equalsIgnoreCase(username)) return "Jose Armando Tunqui (Operario Sede Atocongo)";
        if ("auditor".equalsIgnoreCase(username) || "jchurampi".equalsIgnoreCase(username)) return "Joseph Aaron Churampi (Auditor de Inventario)";
        if ("supervisor".equalsIgnoreCase(username) || "admin".equalsIgnoreCase(username)) return "Anita Condo López (Supervisora Marathon)";
        return username;
    }
}

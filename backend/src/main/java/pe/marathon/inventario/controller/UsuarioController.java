package pe.marathon.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pe.marathon.inventario.model.Usuario;
import pe.marathon.inventario.repository.UsuarioRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador CRUD de Usuarios para el Supervisor (Spring Security & RBAC).
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Usuarios & Control de Acceso", description = "CRUD de usuarios y personal autorizado exclusivo para ROLE_SUPERVISOR")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "Listar todos los usuarios autorizados en la sede")
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un usuario por su ID")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario (Exclusivo ROLE_SUPERVISOR)")
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario) {
        if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
            usuario.setId("user-" + System.currentTimeMillis() % 10000);
        }
        if (usuario.getUsuario() == null || usuario.getUsuario().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario es obligatorio"));
        }
        if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El nombre de usuario ya se encuentra registrado"));
        }
        if (usuario.getClave() != null && !usuario.getClave().startsWith("$2a$")) {
            usuario.setClave(passwordEncoder.encode(usuario.getClave()));
        }
        if (usuario.getSede() == null || usuario.getSede().isEmpty()) {
            usuario.setSede("Marathon Atocongo");
        }
        Usuario guardado = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar datos de un usuario existente")
    public ResponseEntity<?> actualizarUsuario(@PathVariable String id, @RequestBody Usuario datos) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Usuario actual = opt.get();
        if (datos.getNombre() != null) actual.setNombre(datos.getNombre());
        if (datos.getRol() != null) actual.setRol(datos.getRol());
        if (datos.getSede() != null) actual.setSede(datos.getSede());
        if (datos.getCodigo() != null) actual.setCodigo(datos.getCodigo());
        if (datos.getClave() != null && !datos.getClave().trim().isEmpty()) {
            actual.setClave(passwordEncoder.encode(datos.getClave()));
        }

        Usuario actualizado = usuarioRepository.save(actual);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar o dar de baja a un usuario")
    public ResponseEntity<?> eliminarUsuario(@PathVariable String id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado exitosamente del sistema", "id", id));
    }
}

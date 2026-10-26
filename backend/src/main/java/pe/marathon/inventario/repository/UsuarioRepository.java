package pe.marathon.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByUsuario(String usuario);
}

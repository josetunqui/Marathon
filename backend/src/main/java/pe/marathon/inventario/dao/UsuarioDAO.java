package pe.marathon.inventario.dao;

import pe.marathon.inventario.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) para los usuarios y responsables de inventario.
 */
public interface UsuarioDAO {
    List<Usuario> obtenerTodos();
    Optional<Usuario> buscarPorId(String id);
    Optional<Usuario> buscarPorUsuario(String usuario);
    Usuario guardar(Usuario usuario);
}

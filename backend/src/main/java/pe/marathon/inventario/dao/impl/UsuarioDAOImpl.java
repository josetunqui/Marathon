package pe.marathon.inventario.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.marathon.inventario.dao.UsuarioDAO;
import pe.marathon.inventario.model.Usuario;
import pe.marathon.inventario.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioDAOImpl implements UsuarioDAO {

    private final UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> buscarPorUsuario(String usuario) {
        return usuarioRepository.findByUsuario(usuario);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}

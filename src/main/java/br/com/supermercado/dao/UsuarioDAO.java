package br.com.supermercado.dao;

import br.com.supermercado.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioDAO {
    Optional<Usuario> findById(int id);
    Optional<Usuario> findByLogin(String login);
    List<Usuario> findAll();
    Usuario save(Usuario usuario);
    boolean update(Usuario usuario);
    boolean updatePassword(int userId, String newHash); // NOVO MÉTODO
    boolean activateDeactivate(int id, boolean active);
}

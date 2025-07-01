package br.com.supermercado.dao.impl;

import br.com.supermercado.dao.UsuarioDAO;
import br.com.supermercado.db.DatabaseManager;
import br.com.supermercado.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAOImpl implements UsuarioDAO {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAOImpl.class);

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNomeCompleto(rs.getString("nome_completo"));
        usuario.setLogin(rs.getString("login"));
        usuario.setSenhaHash(rs.getString("senha_hash").trim());
        usuario.setPerfil(Usuario.PerfilUsuario.valueOf(rs.getString("perfil").toUpperCase()));
        usuario.setAtivo(rs.getBoolean("ativo"));
        usuario.setDataCadastro(rs.getTimestamp("data_cadastro"));
        usuario.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));
        return usuario;
    }

    @Override
    public Optional<Usuario> findById(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar utilizador por ID: {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findByLogin(String login) {
        String sql = "SELECT * FROM usuarios WHERE login = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar utilizador por login: {}", login, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY nome_completo";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Usuario usuario = mapResultSetToUsuario(rs);
                usuario.setSenhaHash(null);
                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar todos os utilizadores.", e);
        }
        return usuarios;
    }

    @Override
    public Usuario save(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome_completo, login, senha_hash, perfil, ativo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNomeCompleto());
            pstmt.setString(2, usuario.getLogin());
            pstmt.setString(3, usuario.getSenhaHash());
            pstmt.setString(4, usuario.getPerfil().name().toLowerCase());
            pstmt.setBoolean(5, usuario.isAtivo());

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setId(rs.getInt(1));
                        return usuario;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao salvar utilizador: {}", usuario.getLogin(), e);
        }
        return null;
    }

    @Override
    public boolean update(Usuario usuario) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE usuarios SET nome_completo = ?, login = ?, perfil = ?, ativo = ?");
        // A atualização da senha agora é feita por um método dedicado (updatePassword)
        // para evitar alterar a senha acidentalmente aqui.
        sqlBuilder.append(" WHERE id = ?");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            pstmt.setString(1, usuario.getNomeCompleto());
            pstmt.setString(2, usuario.getLogin());
            pstmt.setString(3, usuario.getPerfil().name().toLowerCase());
            pstmt.setBoolean(4, usuario.isAtivo());
            pstmt.setInt(5, usuario.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao atualizar utilizador: {}", usuario.getLogin(), e);
        }
        return false;
    }

    // NOVO MÉTODO IMPLEMENTADO
    @Override
    public boolean updatePassword(int userId, String newHash) {
        String sql = "UPDATE usuarios SET senha_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newHash);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao atualizar a senha para o utilizador ID: {}", userId, e);
        }
        return false;
    }

    @Override
    public boolean activateDeactivate(int id, boolean active) {
        String sql = "UPDATE usuarios SET ativo = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, active);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao {} utilizador ID {}: ", (active ? "ativar" : "desativar"), id, e);
        }
        return false;
    }
}

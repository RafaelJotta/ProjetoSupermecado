package br.com.supermercado.dao.impl;

import br.com.supermercado.dao.CategoriaProdutoDAO;
import br.com.supermercado.db.DatabaseManager;
import br.com.supermercado.model.CategoriaProduto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaProdutoDAOImpl implements CategoriaProdutoDAO {
    private static final Logger logger = LoggerFactory.getLogger(CategoriaProdutoDAOImpl.class);

    @Override
    public List<CategoriaProduto> findAll() {
        List<CategoriaProduto> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categorias_produto ORDER BY nome";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                CategoriaProduto categoria = new CategoriaProduto();
                categoria.setId(rs.getInt("id"));
                categoria.setNome(rs.getString("nome"));
                categorias.add(categoria);
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar todas as categorias.", e);
        }
        return categorias;
    }

    @Override
    public Optional<CategoriaProduto> findById(int id) {
        String sql = "SELECT * FROM categorias_produto WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    CategoriaProduto categoria = new CategoriaProduto();
                    categoria.setId(rs.getInt("id"));
                    categoria.setNome(rs.getString("nome"));
                    return Optional.of(categoria);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar categoria por ID: {}", id, e);
        }
        return Optional.empty();
    }
}
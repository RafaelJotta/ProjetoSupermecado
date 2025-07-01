package br.com.supermercado.dao.impl;

import br.com.supermercado.dao.FornecedorDAO;
import br.com.supermercado.db.DatabaseManager;
import br.com.supermercado.model.Fornecedor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FornecedorDAOImpl implements FornecedorDAO {
    private static final Logger logger = LoggerFactory.getLogger(FornecedorDAOImpl.class);

    @Override
    public List<Fornecedor> findAll() {
        List<Fornecedor> fornecedores = new ArrayList<>();
        String sql = "SELECT * FROM fornecedores ORDER BY nome_fantasia";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Fornecedor fornecedor = new Fornecedor();
                fornecedor.setId(rs.getInt("id"));
                fornecedor.setNomeFantasia(rs.getString("nome_fantasia"));
                fornecedores.add(fornecedor);
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar todos os fornecedores.", e);
        }
        return fornecedores;
    }

    @Override
    public Optional<Fornecedor> findById(int id) {
        String sql = "SELECT * FROM fornecedores WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Fornecedor fornecedor = new Fornecedor();
                    fornecedor.setId(rs.getInt("id"));
                    fornecedor.setNomeFantasia(rs.getString("nome_fantasia"));
                    return Optional.of(fornecedor);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar fornecedor por ID: {}", id, e);
        }
        return Optional.empty();
    }
}
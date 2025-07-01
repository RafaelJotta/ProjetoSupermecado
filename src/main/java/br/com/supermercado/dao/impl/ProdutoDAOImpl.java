package br.com.supermercado.dao.impl;

import br.com.supermercado.dao.ProdutoDAO;
import br.com.supermercado.db.DatabaseManager;
import br.com.supermercado.model.Produto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoDAOImpl implements ProdutoDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoDAOImpl.class);

    private Produto mapResultSetToProduto(ResultSet rs) throws SQLException {
        Produto produto = new Produto();
        produto.setId(rs.getInt("id"));
        produto.setCodigoBarras(rs.getString("codigo_barras"));
        produto.setNome(rs.getString("nome"));
        produto.setDescricao(rs.getString("descricao"));
        produto.setPrecoCompra(rs.getBigDecimal("preco_compra"));
        produto.setPrecoVenda(rs.getBigDecimal("preco_venda"));
        produto.setUnidade(rs.getString("unidade"));
        produto.setQuantidadeEstoque(rs.getInt("quantidade_estoque"));
        produto.setEstoqueMinimo(rs.getInt("estoque_minimo"));
        produto.setIdCategoria(rs.getObject("id_categoria", Integer.class));
        produto.setIdFornecedor(rs.getObject("id_fornecedor", Integer.class));
        produto.setAtivo(rs.getBoolean("ativo"));
        return produto;
    }

    @Override
    public Optional<Produto> findById(int id) {
        String sql = "SELECT * FROM produtos WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduto(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar produto por ID: {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Produto> findByCodigoBarras(String codigoBarras) {
        String sql = "SELECT * FROM produtos WHERE codigo_barras = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoBarras);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduto(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar produto por código de barras: {}", codigoBarras, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Produto> findAll(boolean includeInactive) {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos" + (includeInactive ? "" : " WHERE ativo = TRUE") + " ORDER BY nome";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                produtos.add(mapResultSetToProduto(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar produtos.", e);
        }
        return produtos;
    }

    @Override
    public Produto save(Produto produto) {
        String sql = "INSERT INTO produtos (codigo_barras, nome, descricao, preco_compra, preco_venda, unidade, quantidade_estoque, estoque_minimo, id_categoria, id_fornecedor, ativo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, produto.getCodigoBarras());
            pstmt.setString(2, produto.getNome());
            pstmt.setString(3, produto.getDescricao());
            pstmt.setBigDecimal(4, produto.getPrecoCompra());
            pstmt.setBigDecimal(5, produto.getPrecoVenda());
            pstmt.setString(6, produto.getUnidade());
            pstmt.setInt(7, produto.getQuantidadeEstoque());
            pstmt.setInt(8, produto.getEstoqueMinimo());
            pstmt.setObject(9, produto.getIdCategoria(), Types.INTEGER);
            pstmt.setObject(10, produto.getIdFornecedor(), Types.INTEGER);
            pstmt.setBoolean(11, produto.isAtivo());

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        produto.setId(rs.getInt(1));
                        return produto;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao salvar produto: {}", produto.getNome(), e);
        }
        return null;
    }

    @Override
    public boolean update(Produto produto) {
        String sql = "UPDATE produtos SET codigo_barras = ?, nome = ?, descricao = ?, preco_compra = ?, preco_venda = ?, unidade = ?, quantidade_estoque = ?, estoque_minimo = ?, id_categoria = ?, id_fornecedor = ?, ativo = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, produto.getCodigoBarras());
            pstmt.setString(2, produto.getNome());
            pstmt.setString(3, produto.getDescricao());
            pstmt.setBigDecimal(4, produto.getPrecoCompra());
            pstmt.setBigDecimal(5, produto.getPrecoVenda());
            pstmt.setString(6, produto.getUnidade());
            pstmt.setInt(7, produto.getQuantidadeEstoque());
            pstmt.setInt(8, produto.getEstoqueMinimo());
            pstmt.setObject(9, produto.getIdCategoria(), Types.INTEGER);
            pstmt.setObject(10, produto.getIdFornecedor(), Types.INTEGER);
            pstmt.setBoolean(11, produto.isAtivo());
            pstmt.setInt(12, produto.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao atualizar produto: {}", produto.getNome(), e);
        }
        return false;
    }
}
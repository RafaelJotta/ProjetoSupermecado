package br.com.supermercado.service;

import br.com.supermercado.dao.ProdutoDAO;
import br.com.supermercado.dao.impl.ProdutoDAOImpl;
import br.com.supermercado.model.Produto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProdutoService {

    private final ProdutoDAO produtoDAO;

    public ProdutoService() {
        this.produtoDAO = new ProdutoDAOImpl();
    }

    // Construtor para injeção de dependência (facilita testes)
    public ProdutoService(ProdutoDAO produtoDAO) {
        this.produtoDAO = produtoDAO;
    }

    public Produto salvarOuAtualizar(Produto produto, boolean isNew) throws IllegalArgumentException {
        validarProduto(produto);

        Optional<Produto> produtoExistente = produtoDAO.findByCodigoBarras(produto.getCodigoBarras());
        if (produtoExistente.isPresent() && (isNew || !produtoExistente.get().getId().equals(produto.getId()))) {
            throw new IllegalArgumentException("O código de barras '" + produto.getCodigoBarras() + "' já está em uso.");
        }

        if (isNew) {
            return produtoDAO.save(produto);
        } else {
            if (produtoDAO.update(produto)) {
                return produto;
            }
            return null;
        }
    }

    public List<Produto> buscarTodos(boolean includeInactive) {
        return produtoDAO.findAll(includeInactive);
    }

    public boolean desativarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo.");
        }
        produto.setAtivo(false);
        return produtoDAO.update(produto);
    }

    private void validarProduto(Produto produto) throws IllegalArgumentException {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (produto.getPrecoVenda() == null || produto.getPrecoVenda().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço de venda deve ser maior ou igual a zero.");
        }
        if (produto.getCodigoBarras() == null || produto.getCodigoBarras().trim().isEmpty()) {
            throw new IllegalArgumentException("O código de barras é obrigatório.");
        }
    }
}
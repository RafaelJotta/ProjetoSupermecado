package br.com.supermercado.dao;

import br.com.supermercado.model.Produto;
import java.util.List;
import java.util.Optional;

public interface ProdutoDAO {
    Optional<Produto> findById(int id);
    Optional<Produto> findByCodigoBarras(String codigoBarras);
    List<Produto> findAll(boolean includeInactive);
    Produto save(Produto produto);
    boolean update(Produto produto);
}
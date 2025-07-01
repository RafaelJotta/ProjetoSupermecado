package br.com.supermercado.dao;

import br.com.supermercado.model.CategoriaProduto;
import java.util.List;
import java.util.Optional;

public interface CategoriaProdutoDAO {
    List<CategoriaProduto> findAll();
    Optional<CategoriaProduto> findById(int id);
}
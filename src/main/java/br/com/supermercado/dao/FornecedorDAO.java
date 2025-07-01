package br.com.supermercado.dao;

import br.com.supermercado.model.Fornecedor;
import java.util.List;
import java.util.Optional;

public interface FornecedorDAO {
    List<Fornecedor> findAll();
    Optional<Fornecedor> findById(int id);
}
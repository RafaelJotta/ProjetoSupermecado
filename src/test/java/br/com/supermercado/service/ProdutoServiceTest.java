package br.com.supermercado.service;

import br.com.supermercado.dao.ProdutoDAO;
import br.com.supermercado.model.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoDAO produtoDAO;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produtoValido;

    @BeforeEach
    void setUp() {
        produtoValido = new Produto();
        produtoValido.setId(1);
        produtoValido.setNome("Arroz 5kg");
        produtoValido.setCodigoBarras("789000000001");
        produtoValido.setPrecoVenda(new BigDecimal("25.99"));
        produtoValido.setQuantidadeEstoque(100);
    }

    @Test
    @DisplayName("Deve salvar um novo produto com sucesso quando os dados são válidos")
    void salvarOuAtualizar_DeveSalvarNovoProduto_QuandoDadosValidos() {
        when(produtoDAO.findByCodigoBarras(produtoValido.getCodigoBarras())).thenReturn(Optional.empty());
        when(produtoDAO.save(produtoValido)).thenReturn(produtoValido);

        Produto produtoSalvo = produtoService.salvarOuAtualizar(produtoValido, true);

        assertNotNull(produtoSalvo);
        assertEquals("Arroz 5kg", produtoSalvo.getNome());
        verify(produtoDAO, times(1)).save(produtoValido);
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar produto com nome vazio")
    void salvarOuAtualizar_DeveLancarExcecao_QuandoNomeVazio() {
        produtoValido.setNome("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            produtoService.salvarOuAtualizar(produtoValido, true);
        });

        assertEquals("O nome do produto é obrigatório.", exception.getMessage());
        verify(produtoDAO, never()).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar novo produto com código de barras já existente")
    void salvarOuAtualizar_DeveLancarExcecao_QuandoCodigoBarrasJaExisteEmNovoProduto() {
        when(produtoDAO.findByCodigoBarras(produtoValido.getCodigoBarras())).thenReturn(Optional.of(produtoValido));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            produtoService.salvarOuAtualizar(produtoValido, true);
        });

        assertTrue(exception.getMessage().contains("já está em uso"));
        verify(produtoDAO, never()).save(any(Produto.class));
    }
}

package br.com.supermercado.controller;

import br.com.supermercado.dao.CategoriaProdutoDAO;
import br.com.supermercado.dao.FornecedorDAO;
import br.com.supermercado.dao.impl.CategoriaProdutoDAOImpl;
import br.com.supermercado.dao.impl.FornecedorDAOImpl;
import br.com.supermercado.model.CategoriaProduto;
import br.com.supermercado.model.Fornecedor;
import br.com.supermercado.model.Produto;
import br.com.supermercado.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class ProductFormController {

    private static final Logger logger = LoggerFactory.getLogger(ProductFormController.class);

    @FXML private Label titleLabel;
    @FXML private TextField nomeField, codigoBarrasField, precoCompraField, precoVendaField, unidadeField, estoqueField, estoqueMinimoField;
    @FXML private TextArea descricaoArea;
    @FXML private ComboBox<CategoriaProduto> categoriaComboBox;
    @FXML private ComboBox<Fornecedor> fornecedorComboBox;

    private ProdutoService produtoService = new ProdutoService();
    private CategoriaProdutoDAO categoriaDAO = new CategoriaProdutoDAOImpl();
    private FornecedorDAO fornecedorDAO = new FornecedorDAOImpl();

    private Produto produto;
    private boolean isNew;

    @FXML
    public void initialize() {
        loadComboBoxes();
    }

    private void loadComboBoxes() {
        categoriaComboBox.setItems(FXCollections.observableArrayList(categoriaDAO.findAll()));
        fornecedorComboBox.setItems(FXCollections.observableArrayList(fornecedorDAO.findAll()));

        categoriaComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(CategoriaProduto c) { return c == null ? null : c.getNome(); }
            @Override public CategoriaProduto fromString(String s) { return null; }
        });

        fornecedorComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Fornecedor f) { return f == null ? null : f.getNomeFantasia(); }
            @Override public Fornecedor fromString(String s) { return null; }
        });
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
        this.isNew = (produto == null);
        if (isNew) {
            this.produto = new Produto();
            titleLabel.setText("Adicionar Novo Produto");
        } else {
            titleLabel.setText("Editar Produto");
            populateForm();
        }
    }

    private void populateForm() {
        nomeField.setText(produto.getNome());
        codigoBarrasField.setText(produto.getCodigoBarras());
        descricaoArea.setText(produto.getDescricao());
        precoCompraField.setText(produto.getPrecoCompra() != null ? produto.getPrecoCompra().toPlainString() : "");
        precoVendaField.setText(produto.getPrecoVenda() != null ? produto.getPrecoVenda().toPlainString() : "");
        unidadeField.setText(produto.getUnidade());
        estoqueField.setText(String.valueOf(produto.getQuantidadeEstoque()));
        estoqueMinimoField.setText(String.valueOf(produto.getEstoqueMinimo()));

        if (produto.getIdCategoria() != null) {
            categoriaDAO.findById(produto.getIdCategoria()).ifPresent(c -> categoriaComboBox.setValue(c));
        }
        if (produto.getIdFornecedor() != null) {
            fornecedorDAO.findById(produto.getIdFornecedor()).ifPresent(f -> fornecedorComboBox.setValue(f));
        }
    }

    @FXML
    private void handleSalvar(ActionEvent event) {
        try {
            produto.setNome(nomeField.getText());
            produto.setCodigoBarras(codigoBarrasField.getText());
            produto.setDescricao(descricaoArea.getText());
            produto.setPrecoCompra(new BigDecimal(precoCompraField.getText().replace(",", ".")));
            produto.setPrecoVenda(new BigDecimal(precoVendaField.getText().replace(",", ".")));
            produto.setUnidade(unidadeField.getText());
            produto.setQuantidadeEstoque(Integer.parseInt(estoqueField.getText()));
            produto.setEstoqueMinimo(Integer.parseInt(estoqueMinimoField.getText()));
            produto.setAtivo(isNew || produto.isAtivo());

            CategoriaProduto cat = categoriaComboBox.getValue();
            produto.setIdCategoria(cat != null ? cat.getId() : null);

            Fornecedor forn = fornecedorComboBox.getValue();
            produto.setIdFornecedor(forn != null ? forn.getId() : null);

            produtoService.salvarOuAtualizar(produto, isNew);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Produto salvo com sucesso!");
            closeStage();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Verifique se os campos numéricos (preços, estoque) estão corretos.");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Dados Inválidos", e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        closeStage();
    }

    private void closeStage() {
        ((Stage) nomeField.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

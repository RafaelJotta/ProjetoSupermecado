package br.com.supermercado.controller;

import br.com.supermercado.model.Produto;
import br.com.supermercado.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class ProductManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ProductManagementController.class);

    @FXML private TableView<Produto> productTableView;
    @FXML private TableColumn<Produto, Integer> idColumn;
    @FXML private TableColumn<Produto, String> codigoBarrasColumn;
    @FXML private TableColumn<Produto, String> nomeColumn;
    @FXML private TableColumn<Produto, BigDecimal> precoVendaColumn;
    @FXML private TableColumn<Produto, Integer> estoqueColumn;
    @FXML private TableColumn<Produto, String> statusColumn;
    @FXML private TextField searchField;
    @FXML private CheckBox showInactiveCheckBox;

    private ProdutoService produtoService = new ProdutoService();
    private ObservableList<Produto> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadProducts();
        setupFilterAndSort();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        codigoBarrasColumn.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        precoVendaColumn.setCellValueFactory(new PropertyValueFactory<>("precoVenda"));
        estoqueColumn.setCellValueFactory(new PropertyValueFactory<>("quantidadeEstoque"));
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().isAtivo() ? "Ativo" : "Inativo"));
    }

    private void loadProducts() {
        try {
            masterData.setAll(produtoService.buscarTodos(true));
            productTableView.refresh();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a lista de produtos.");
        }
    }

    private void setupFilterAndSort() {
        FilteredList<Produto> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, old, val) -> applyFilter(filteredData));
        showInactiveCheckBox.selectedProperty().addListener((obs, old, val) -> applyFilter(filteredData));

        applyFilter(filteredData);

        SortedList<Produto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTableView.comparatorProperty());
        productTableView.setItems(sortedData);
    }

    private void applyFilter(FilteredList<Produto> filteredData) {
        String searchText = searchField.getText();
        boolean showInactive = showInactiveCheckBox.isSelected();

        filteredData.setPredicate(produto -> {
            boolean matchesActivity = showInactive || produto.isAtivo();
            boolean matchesSearch = searchText == null || searchText.isEmpty() || produto.getNome().toLowerCase().contains(searchText.toLowerCase());
            return matchesActivity && matchesSearch;
        });
    }

    @FXML private void handleAdicionar() { showProductForm(null); }

    @FXML private void handleEditar() {
        Produto selected = productTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um produto para editar.");
            return;
        }
        showProductForm(selected);
    }

    @FXML private void handleDesativar() {
        Produto selected = productTableView.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isAtivo()) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um produto ativo para desativar.");
            return;
        }

        Optional<ButtonType> result = mostrarConfirmacao("Confirmar Desativação", "Deseja realmente desativar o produto '" + selected.getNome() + "'?");
        if (result.isPresent() && result.get() == ButtonType.YES) {
            produtoService.desativarProduto(selected);
            loadProducts();
        }
    }

    private void showProductForm(Produto produto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/com/supermercado/fxml/ProductFormDialog.fxml"));
            Parent root = loader.load();

            ProductFormController controller = loader.getController();
            controller.setProduto(produto);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(produto == null ? "Novo Produto" : "Editar Produto");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(productTableView.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            dialogStage.showAndWait();
            loadProducts();

        } catch (IOException e) {
            logger.error("Falha ao abrir formulário de produto", e);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> mostrarConfirmacao(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        return alert.showAndWait();
    }
}

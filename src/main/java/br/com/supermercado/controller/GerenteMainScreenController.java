package br.com.supermercado.controller;

import br.com.supermercado.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GerenteMainScreenController {

    private static final Logger logger = LoggerFactory.getLogger(GerenteMainScreenController.class);
    @FXML private BorderPane mainBorderPane;

    @FXML
    private void handleGerenciarUsuarios(ActionEvent event) {
        logger.info("A carregar o ecrã de Gerenciamento de Utilizadores...");
        loadUI("/br/com/supermercado/fxml/UserManagementScreen.fxml");
    }

    @FXML
    private void handleGerenciarProdutos(ActionEvent event) {
        logger.info("A carregar o ecrã de Gerenciamento de Produtos...");
        loadUI("/br/com/supermercado/fxml/ProductManagementScreen.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LoginScreenController.doLogout();
        MainApp.showLoginScreen();
    }

    @FXML
    private void handleSair(ActionEvent event) {
        logger.info("Ação: Sair da aplicação.");
        System.exit(0);
    }

    // MÉTODOS ADICIONADOS PARA CORRIGIR O ERRO
    @FXML private void handleGerenciarCategorias(ActionEvent event) { showAlert("Funcionalidade em Desenvolvimento", "A gestão de categorias será implementada em breve."); }
    @FXML private void handleGerenciarFornecedores(ActionEvent event) { showAlert("Funcionalidade em Desenvolvimento", "A gestão de fornecedores será implementada em breve."); }
    @FXML private void handleAjustarEstoque(ActionEvent event) { showAlert("Funcionalidade em Desenvolvimento", "O ajuste de estoque será implementado em breve."); }
    @FXML private void handleRelatorioVendas(ActionEvent event) { showAlert("Funcionalidade em Desenvolvimento", "O relatório de vendas será implementado em breve."); }
    @FXML private void handleRelatorioEstoqueBaixo(ActionEvent event) { showAlert("Funcionalidade em Desenvolvimento", "O relatório de estoque baixo será implementado em breve."); }

    private void loadUI(String fxmlPath) {
        try {
            Parent uiRoot = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainBorderPane.setCenter(uiRoot);
        } catch (IOException e) {
            logger.error("Falha ao carregar o FXML: {}", fxmlPath, e);
            showAlert("Erro Crítico", "Não foi possível carregar o ecrã solicitado.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

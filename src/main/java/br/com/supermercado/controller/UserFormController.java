package br.com.supermercado.controller;

import br.com.supermercado.model.Usuario;
import br.com.supermercado.service.UsuarioService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserFormController {

    private static final Logger logger = LoggerFactory.getLogger(UserFormController.class);

    @FXML private Label titleLabel;
    @FXML private TextField nomeField;
    @FXML private TextField loginField;
    @FXML private PasswordField senhaField;
    @FXML private ComboBox<Usuario.PerfilUsuario> perfilComboBox;
    @FXML private Label senhaLabel;
    @FXML private Label infoSenhaLabel;

    private UsuarioService usuarioService;
    private Usuario usuario;
    private boolean isNewUser;

    public UserFormController() {
        this.usuarioService = new UsuarioService();
    }

    @FXML
    public void initialize() {
        perfilComboBox.setItems(FXCollections.observableArrayList(Usuario.PerfilUsuario.values()));
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.isNewUser = (usuario == null);
        configurarFormulario();
    }

    private void configurarFormulario() {
        if (isNewUser) {
            this.usuario = new Usuario();
            titleLabel.setText("Adicionar Novo Utilizador");
            senhaLabel.setText("Senha (obrigatória):");
            infoSenhaLabel.setVisible(false);
            this.usuario.setAtivo(true);
        } else {
            titleLabel.setText("Editar Utilizador");
            nomeField.setText(usuario.getNomeCompleto());
            loginField.setText(usuario.getLogin());
            perfilComboBox.setValue(usuario.getPerfil());
            senhaLabel.setText("Nova Senha:");
            infoSenhaLabel.setVisible(true);
        }
    }

    @FXML
    void handleSalvar(ActionEvent event) {
        try {
            usuario.setNomeCompleto(nomeField.getText());
            usuario.setLogin(loginField.getText());
            usuario.setPerfil(perfilComboBox.getValue());

            String senhaPlainText = senhaField.getText();

            if (usuarioService.salvarOuAtualizarUsuario(usuario, isNewUser, senhaPlainText)) {
                logger.info("Utilizador '{}' salvo/atualizado com sucesso.", usuario.getLogin());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Utilizador salvo com sucesso!");
                fecharJanela();
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Erro de validação ao salvar utilizador: {}", e.getMessage());
            mostrarAlerta(Alert.AlertType.WARNING, "Dados Inválidos", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar utilizador.", e);
            mostrarAlerta(Alert.AlertType.ERROR, "Erro Crítico", "Ocorreu um erro inesperado ao salvar. Verifique os logs.");
        }
    }

    @FXML
    void handleCancelar(ActionEvent event) {
        fecharJanela();
    }

    private void fecharJanela() {
        ((Stage) nomeField.getScene().getWindow()).close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
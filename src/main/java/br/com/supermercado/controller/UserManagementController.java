package br.com.supermercado.controller;

import br.com.supermercado.model.Usuario;
import br.com.supermercado.service.UsuarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    @FXML private TableView<Usuario> userTableView;
    @FXML private TableColumn<Usuario, Integer> idColumn;
    @FXML private TableColumn<Usuario, String> nomeColumn;
    @FXML private TableColumn<Usuario, String> loginColumn;
    @FXML private TableColumn<Usuario, Usuario.PerfilUsuario> perfilColumn;
    @FXML private TableColumn<Usuario, String> statusColumn;

    private UsuarioService usuarioService;
    private ObservableList<Usuario> usuariosList;

    public UserManagementController() {
        this.usuarioService = new UsuarioService();
    }

    @FXML
    public void initialize() {
        logger.info("A inicializar o ecrã de Gerenciamento de Utilizadores.");
        configurarColunas();
        carregarUsuarios();
    }

    private void configurarColunas() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nomeCompleto"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        perfilColumn.setCellValueFactory(new PropertyValueFactory<>("perfil"));
        statusColumn.setCellValueFactory(cellData -> {
            boolean ativo = cellData.getValue().isAtivo();
            return new javafx.beans.property.SimpleStringProperty(ativo ? "Ativo" : "Inativo");
        });
    }

    private void carregarUsuarios() {
        try {
            usuariosList = FXCollections.observableArrayList(usuarioService.buscarTodosUsuarios());
            userTableView.setItems(usuariosList);
            logger.info("Lista de utilizadores carregada com {} registos.", usuariosList.size());
        } catch (Exception e) {
            logger.error("Erro ao carregar lista de utilizadores.", e);
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar os utilizadores do banco de dados.");
        }
    }

    @FXML
    void handleAdicionarUsuario(ActionEvent event) {
        logger.debug("Botão 'Adicionar' clicado.");
        abrirFormularioUsuario(null);
    }

    @FXML
    void handleEditarUsuario(ActionEvent event) {
        Usuario selecionado = userTableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nenhum utilizador selecionado", "Por favor, selecione um utilizador na tabela para editar.");
            return;
        }
        logger.debug("Botão 'Editar' clicado para o utilizador ID: {}", selecionado.getId());
        abrirFormularioUsuario(selecionado);
    }

    @FXML
    void handleAtivarDesativarUsuario(ActionEvent event) {
        Usuario selecionado = userTableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,"Nenhum utilizador selecionado", "Por favor, selecione um utilizador na tabela para ativar ou desativar.");
            return;
        }

        boolean ativar = !selecionado.isAtivo();
        String acao = ativar ? "ativar" : "desativar";
        String confirmacaoMsg = String.format("Tem a certeza que deseja %s o utilizador '%s'?", acao, selecionado.getNomeCompleto());

        Optional<ButtonType> result = mostrarConfirmacao("Confirmar Ação", confirmacaoMsg);

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                if (usuarioService.ativarDesativarUsuario(selecionado.getId(), ativar)) {
                    logger.info("Utilizador ID {} {} com sucesso.", selecionado.getId(), acao);
                    carregarUsuarios(); // Recarrega a lista para mostrar o novo status
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR,"Erro", "A operação não pôde ser concluída.");
                }
            } catch (IllegalStateException e) {
                logger.warn("Tentativa de operação ilegal: {}", e.getMessage());
                mostrarAlerta(Alert.AlertType.WARNING,"Operação não permitida", e.getMessage());
            } catch (Exception e) {
                logger.error("Erro ao " + acao + " utilizador.", e);
                mostrarAlerta(Alert.AlertType.ERROR,"Erro", "Ocorreu um erro ao tentar " + acao + " o utilizador.");
            }
        }
    }

    private void abrirFormularioUsuario(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/com/supermercado/fxml/UserFormDialog.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.setUsuario(usuario);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(usuario == null ? "Adicionar Novo Utilizador" : "Editar Utilizador");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userTableView.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            dialogStage.showAndWait();

            carregarUsuarios();
        } catch (IOException e) {
            logger.error("Falha ao abrir o formulário de utilizador.", e);
            mostrarAlerta(Alert.AlertType.ERROR, "Erro Crítico", "Não foi possível abrir o ecrã de cadastro de utilizador.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private Optional<ButtonType> mostrarConfirmacao(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        return alert.showAndWait();
    }
}

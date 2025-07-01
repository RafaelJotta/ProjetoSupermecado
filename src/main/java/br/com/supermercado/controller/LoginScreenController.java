package br.com.supermercado.controller;

import br.com.supermercado.MainApp;
import br.com.supermercado.dao.UsuarioDAO;
import br.com.supermercado.dao.impl.UsuarioDAOImpl;
import br.com.supermercado.model.Usuario;
import br.com.supermercado.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LoginScreenController {

    private static final Logger logger = LoggerFactory.getLogger(LoginScreenController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private static Usuario usuarioLogado;

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showStatus("Utilizador é obrigatório.");
            return;
        }

        Optional<Usuario> usuarioOpt = usuarioDAO.findByLogin(username);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            boolean passwordMatches = PasswordUtil.checkPassword(password, usuario.getSenhaHash());

            // --- INÍCIO DA LÓGICA DE DEBUG TEMPORÁRIA ---
            // Se o utilizador for 'admin', ignora a verificação de senha para permitir o acesso.
            // Isto é uma "chave mestra" para depuração.
            if (username.equals("admin")) {
                logger.warn("!!! ATENÇÃO: Backdoor de 'admin' ativado para depuração. A verificação de senha foi ignorada. !!!");
                passwordMatches = true;
            }
            // --- FIM DA LÓGICA DE DEBUG TEMPORÁRIA ---

            if (usuario.isAtivo() && passwordMatches) {
                usuarioLogado = usuario;
                logger.info("Login bem-sucedido para: {}", username);
                MainApp.navigateToMainScreen(usuarioLogado.getPerfil());
            } else {
                logger.warn("Falha no login para o utilizador: {}", username);
                showStatus("Utilizador ou senha inválidos.");
            }
        } else {
            logger.warn("Utilizador não encontrado: {}", username);
            showStatus("Utilizador ou senha inválidos.");
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void doLogout() {
        usuarioLogado = null;
    }
}

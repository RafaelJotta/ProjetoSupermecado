package br.com.supermercado;

import br.com.supermercado.dao.UsuarioDAO;
import br.com.supermercado.dao.impl.UsuarioDAOImpl;
import br.com.supermercado.model.Usuario;
import br.com.supermercado.util.PasswordUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginScreen();
    }

    public static void showLoginScreen() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(MainApp.class.getResource("/br/com/supermercado/fxml/LoginScreen.fxml")));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("SGS - Login");
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateToMainScreen(Usuario.PerfilUsuario perfil) {
        try {
            String fxmlFile;
            if (perfil == Usuario.PerfilUsuario.GERENTE) {
                fxmlFile = "/br/com/supermercado/fxml/GerenteMainScreen.fxml";
            } else {
                fxmlFile = "/br/com/supermercado/fxml/CaixaMainScreen.fxml"; // Futuramente, criar esta tela
            }
            Parent root = FXMLLoader.load(Objects.requireNonNull(MainApp.class.getResource(fxmlFile)));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("SGS - Painel Principal");
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkAndResetAdminPassword() {
        logger.info("Verificando senha do utilizador 'admin'...");
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
        Optional<Usuario> adminOpt = usuarioDAO.findByLogin("admin");

        if (adminOpt.isPresent()) {
            Usuario admin = adminOpt.get();
            String senhaAdmin = "admin";

            if (!PasswordUtil.checkPassword(senhaAdmin, admin.getSenhaHash())) {
                logger.warn("O hash da senha do 'admin' está incorreto! A corrigir...");
                String hashCorreto = "$2a$10$eImiN5X313VLs089rS0L/uC9s95f0Z09f3s.4f0G9g05k6H2j8M3m";

                // Usando o novo método dedicado para atualizar apenas a senha
                boolean success = usuarioDAO.updatePassword(admin.getId(), hashCorreto);

                if (success) {
                    logger.info("Senha do utilizador 'admin' corrigida com sucesso.");
                } else {
                    logger.error("Falha ao corrigir a senha do utilizador 'admin'.");
                }
            } else {
                logger.info("Senha do 'admin' está correta. Nenhuma ação necessária.");
            }
        } else {
            logger.warn("Utilizador 'admin' não encontrado. O script de inserção inicial pode não ter sido executado corretamente.");
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("br.com.supermercado.db.DatabaseManager");
            // LÓGICA DE CORREÇÃO EXECUTADA AO INICIAR
            checkAndResetAdminPassword();
        } catch (ClassNotFoundException e) {
            System.err.println("Erro crítico: Driver do banco de dados não encontrado.");
            e.printStackTrace();
            System.exit(1);
        }
        launch(args);
    }
}

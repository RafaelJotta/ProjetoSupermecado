package br.com.supermercado.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Classe responsável por gerenciar a conexão com o banco de dados
 * e inicializar o esquema de tabelas.
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    // --- IMPORTANTE: Altere as credenciais abaixo para as do seu banco de dados ---
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "supermercado_db";
    private static final String DB_USER = "root"; // <-- SUBSTITUA AQUI
    private static final String DB_PASSWORD = ""; // <-- SUBSTITUA AQUI
    // -----------------------------------------------------------------------------

    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String SCHEMA_PATH = "/br/com/supermercado/database/schema.sql";
    private static boolean schemaInitialized = false;

    static {
        try {
            Class.forName(JDBC_DRIVER);
            logger.info("Driver JDBC do MySQL carregado com sucesso.");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            logger.error("Driver JDBC do MySQL não encontrado.", e);
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static synchronized void initializeDatabase() {
        if (schemaInitialized) return;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            boolean tablesExist = false;
            try (var rs = stmt.executeQuery("SHOW TABLES LIKE 'usuarios'")) {
                if (rs.next()) {
                    tablesExist = true;
                    logger.info("Banco de dados já parece estar inicializado.");
                }
            }

            if (!tablesExist) {
                logger.info("Inicializando esquema de tabelas...");
                try (InputStream is = DatabaseManager.class.getResourceAsStream(SCHEMA_PATH)) {
                    if (is == null) throw new RuntimeException("Arquivo schema.sql não encontrado no caminho: " + SCHEMA_PATH);

                    String schemaSql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                    String[] statements = schemaSql.split(";(?=(?:[^']*'[^']*')*[^']*$)");

                    for (String sql : statements) {
                        if (sql != null && !sql.trim().isEmpty()) {
                            stmt.execute(sql.trim());
                        }
                    }
                    logger.info("Esquema de tabelas inicializado com sucesso.");
                } catch (Exception e) {
                    logger.error("Erro ao executar o script schema.sql.", e);
                    throw new RuntimeException(e);
                }
            }
            schemaInitialized = true;
        } catch (SQLException e) {
            logger.error("Falha ao inicializar o banco de dados. Verifique suas credenciais de usuário/senha em DatabaseManager.java e se o servidor MySQL está rodando.", e);
            throw new RuntimeException(e);
        }
    }
}
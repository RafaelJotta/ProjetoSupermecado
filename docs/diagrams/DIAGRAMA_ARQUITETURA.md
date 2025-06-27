Diagrama de Classes - Arquitetura MVC + DAO
Este diagrama ilustra a arquitetura principal do SGS (Sistema de Gerenciamento de Supermercado), mostrando a separação de responsabilidades entre as camadas de View, Controller, Model (com Entidades e Serviços) e DAO.
    
    %%{init: {'theme': 'default'}}%%
    classDiagram
        direction TB
    %% VIEW (FXML)
    class UserManagementScreen_FXML {
        <<FXML>>
    }
    class ProductManagementScreen_FXML {
        <<FXML>>
    }
    class LoginScreen_FXML {
        <<FXML>>
    }

    %% CONTROLLER
    class UserManagementController {
        +userTableView: TableView
        -usuarioService: UsuarioService
        +initialize()
        +handleAdicionarUsuario()
        +handleEditarUsuario()
    }
    class ProductManagementController {
        +productTableView: TableView
        -produtoService: ProdutoService
        +initialize()
        +handleAdicionar()
        +handleEditar()
    }
    class LoginScreenController {
        -usernameField: TextField
        -passwordField: PasswordField
        -usuarioService: UsuarioService
        +handleLoginButtonAction()
    }

    %% MODEL
    class Usuario {
        <<Entity>>
        -id: Integer
        -nomeCompleto: String
        -login: String
        -perfil: PerfilUsuario
        -ativo: boolean
    }
    class Produto {
        <<Entity>>
        -id: Integer
        -nome: String
        -precoVenda: BigDecimal
        -quantidadeEstoque: int
    }
    class UsuarioService {
        <<Service>>
        -usuarioDAO: UsuarioDAO
        +salvarOuAtualizarUsuario(...)
        +buscarTodosUsuarios()
        +ativarDesativarUsuario(...)
    }
    class ProdutoService {
        <<Service>>
        -produtoDAO: ProdutoDAO
        +salvarOuAtualizar(...)
        +buscarTodos(...)
    }

    %% DAO (Data Access Object)
    class UsuarioDAO {
        <<Interface>>
        +findById(id)
        +findAll()
        +save(usuario)
        +update(usuario)
    }
    class UsuarioDAOImpl {
        +findById(id)
        +findAll()
        +save(usuario)
        +update(usuario)
    }
    class ProdutoDAO {
        <<Interface>>
        +findById(id)
        +findAll()
        +save(produto)
        +update(produto)
    }
    class ProdutoDAOImpl {
        +findById(id)
        +findAll()
        +save(produto)
        +update(produto)
    }

    %% PERSISTENCE HELPER
    class DatabaseManager {
        +getConnection(): Connection
    }

    %% RELACIONAMENTOS
    %% View <--> Controller (O Controller gerencia a View)
    UserManagementScreen_FXML ..> UserManagementController : "é controlada por"
    ProductManagementScreen_FXML ..> ProductManagementController : "é controlada por"
    LoginScreen_FXML ..> LoginScreenController : "é controlada por"

    %% Controller --> Service (O Controller usa o Service para executar ações)
    UserManagementController --> UsuarioService : "usa"
    ProductManagementController --> ProdutoService : "usa"
    LoginScreenController --> UsuarioService : "usa"

    %% Service --> DAO Interface (O Service usa a interface do DAO)
    UsuarioService --> UsuarioDAO : "usa"
    ProdutoService --> ProdutoDAO : "usa"

    %% DAO Impl --|> DAO Interface (A Implementação realiza o contrato da Interface)
    UsuarioDAOImpl ..|> UsuarioDAO : "implementa"
    ProdutoDAOImpl ..|> ProdutoDAO : "implementa"

    %% DAO Impl --> DatabaseManager (A Implementação usa o Gerenciador de Conexão)
    UsuarioDAOImpl --> DatabaseManager : "usa"
    ProdutoDAOImpl --> DatabaseManager : "usa"

    %% Model (Entities) são usadas por todas as camadas
    UserManagementController ..> Usuario : "manipula"
    UsuarioService ..> Usuario : "processa"
    UsuarioDAOImpl ..> Usuario : "persiste"

    ProductManagementController ..> Produto : "manipula"
    ProdutoService ..> Produto : "processa"
    ProdutoDAOImpl ..> Produto : "persiste"

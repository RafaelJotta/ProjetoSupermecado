-- Script de Criação de Tabelas para o SGS
-- Banco de Dados: MySQL

CREATE DATABASE IF NOT EXISTS supermercado_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE supermercado_db;

-- Tabela de Usuários
CREATE TABLE IF NOT EXISTS usuarios (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        nome_completo VARCHAR(255) NOT NULL,
    login VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil ENUM('caixa', 'gerente') NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

-- Tabela de Categorias de Produto
CREATE TABLE IF NOT EXISTS categorias_produto (
                                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                                  nome VARCHAR(100) NOT NULL UNIQUE
    ) ENGINE=InnoDB;

-- Tabela de Fornecedores
CREATE TABLE IF NOT EXISTS fornecedores (
                                            id INT AUTO_INCREMENT PRIMARY KEY,
                                            nome_fantasia VARCHAR(255) NOT NULL UNIQUE,
    razao_social VARCHAR(255),
    cnpj VARCHAR(18) UNIQUE,
    contato VARCHAR(255)
    ) ENGINE=InnoDB;

-- Tabela de Produtos
CREATE TABLE IF NOT EXISTS produtos (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        codigo_barras VARCHAR(100) UNIQUE,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco_compra DECIMAL(10, 2) NOT NULL,
    preco_venda DECIMAL(10, 2) NOT NULL,
    unidade VARCHAR(20) NOT NULL,
    quantidade_estoque INT NOT NULL DEFAULT 0,
    estoque_minimo INT NOT NULL DEFAULT 0,
    id_categoria INT,
    id_fornecedor INT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES categorias_produto(id) ON DELETE SET NULL,
    FOREIGN KEY (id_fornecedor) REFERENCES fornecedores(id) ON DELETE SET NULL
    ) ENGINE=InnoDB;

-- Tabela de Sessões do Caixa
CREATE TABLE IF NOT EXISTS sessoes_caixa (
                                             id INT AUTO_INCREMENT PRIMARY KEY,
                                             id_usuario_caixa INT NOT NULL,
                                             data_abertura TIMESTAMP NOT NULL,
                                             valor_inicial_caixa DECIMAL(10, 2) NOT NULL,
    data_fechamento TIMESTAMP NULL,
    valor_final_apurado_sistema DECIMAL(10, 2),
    valor_final_contado_gaveta DECIMAL(10, 2),
    diferenca DECIMAL(10, 2),
    status_sessao ENUM('aberta', 'fechada') NOT NULL DEFAULT 'aberta',
    FOREIGN KEY (id_usuario_caixa) REFERENCES usuarios(id)
    ) ENGINE=InnoDB;

-- Tabela de Vendas
CREATE TABLE IF NOT EXISTS vendas (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      id_usuario_caixa INT NOT NULL,
                                      id_sessao_caixa INT,
                                      valor_total_bruto DECIMAL(10, 2) NOT NULL,
    valor_desconto_total DECIMAL(10, 2) DEFAULT 0.00,
    valor_total_liquido DECIMAL(10, 2) NOT NULL,
    status_venda ENUM('em_aberto', 'concluida', 'cancelada') NOT NULL,
    FOREIGN KEY (id_usuario_caixa) REFERENCES usuarios(id),
    FOREIGN KEY (id_sessao_caixa) REFERENCES sessoes_caixa(id)
    ) ENGINE=InnoDB;

-- Tabela de Itens da Venda
CREATE TABLE IF NOT EXISTS itens_venda (
                                           id INT AUTO_INCREMENT PRIMARY KEY,
                                           id_venda INT NOT NULL,
                                           id_produto INT NOT NULL,
                                           quantidade INT NOT NULL,
                                           preco_unitario_venda DECIMAL(10, 2) NOT NULL,
    subtotal_item DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (id_venda) REFERENCES vendas(id) ON DELETE CASCADE,
    FOREIGN KEY (id_produto) REFERENCES produtos(id) ON DELETE RESTRICT
    ) ENGINE=InnoDB;

-- Tabela de Movimentações de Estoque
CREATE TABLE IF NOT EXISTS movimentacoes_estoque (
                                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                                     id_produto INT NOT NULL,
                                                     id_usuario_responsavel INT,
                                                     id_venda_origem INT NULL,
                                                     tipo_movimentacao ENUM('entrada_compra', 'venda_cliente', 'ajuste_entrada',
                                                     'ajuste_saida', 'baixa_perda', 'baixa_vencimento', 'devolucao_cliente') NOT NULL,
    quantidade INT NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacao TEXT,
    FOREIGN KEY (id_produto) REFERENCES produtos(id),
    FOREIGN KEY (id_usuario_responsavel) REFERENCES usuarios(id),
    FOREIGN KEY (id_venda_origem) REFERENCES vendas(id)
    ) ENGINE=InnoDB;

-- Dados Iniciais
INSERT IGNORE INTO usuarios (nome_completo, login, senha_hash, perfil, ativo)
VALUES ('Administrador do Sistema', 'admin', '$2a$10$eImiN5X313VLs089rS0L/uC9s95f0Z09f3s.4f0G9g05k6H2j8M3m', 'gerente', TRUE); -- Senha: admin

INSERT IGNORE INTO categorias_produto (nome) VALUES ('Mercearia'), ('Bebidas'), ('Hortifruti'), ('Limpeza'), ('Higiene');
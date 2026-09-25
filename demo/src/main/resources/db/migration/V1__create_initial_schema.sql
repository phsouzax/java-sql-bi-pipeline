CREATE TABLE empresa (
    cnpj VARCHAR(14) PRIMARY KEY,
    razao_social VARCHAR(255) NOT NULL,
    porte VARCHAR(50),
    natureza_juridica VARCHAR(255),
    situacao_cadastral VARCHAR(50),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE veiculo_fipe (
    codigo_fipe VARCHAR(20) PRIMARY KEY,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    ano_modelo INT NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    mes_referencia VARCHAR(20) NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE log_ingestao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_ingestao VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    mensagem_erro TEXT,
    data_execucao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
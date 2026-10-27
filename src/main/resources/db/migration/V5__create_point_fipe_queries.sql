CREATE TABLE consulta_preco_fipe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_veiculo VARCHAR(20) NOT NULL,
    codigo_fipe VARCHAR(20) NOT NULL,
    codigo_ano VARCHAR(20) NOT NULL,
    codigo_referencia VARCHAR(10) NOT NULL,
    marca VARCHAR(120) NOT NULL,
    modelo VARCHAR(255) NOT NULL,
    combustivel VARCHAR(60),
    sigla_combustivel VARCHAR(5),
    ano_modelo INT,
    codigo_tipo_fipe INT,
    valor DECIMAL(15, 2) NOT NULL,
    mes_referencia VARCHAR(50) NOT NULL,
    consultado_em TIMESTAMP NOT NULL,
    CONSTRAINT uk_consulta_preco_fipe UNIQUE (tipo_veiculo, codigo_fipe, codigo_ano, codigo_referencia)
);

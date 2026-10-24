CREATE TABLE fato_preco_fipe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_fipe VARCHAR(20) NOT NULL,
    mes_referencia VARCHAR(50) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_veiculo_mes UNIQUE (codigo_fipe, mes_referencia),
    CONSTRAINT fk_veiculo FOREIGN KEY (codigo_fipe) REFERENCES veiculo_fipe(codigo_fipe)
);
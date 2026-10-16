CREATE TABLE fato_preco_fipe (
    id BIGSERIAL PRIMARY KEY,
    veiculo_id BIGINT NOT NULL,
    mes_referencia VARCHAR(50) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_veiculo_mes UNIQUE (veiculo_id, mes_referencia),
    CONSTRAINT fk_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculo_fipe(id)
);
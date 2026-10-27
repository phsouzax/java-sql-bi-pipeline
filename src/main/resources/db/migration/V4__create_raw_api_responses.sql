CREATE TABLE raw_cnpj (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chave VARCHAR(255) NOT NULL,
    json_original TEXT NOT NULL,
    data_chamada TIMESTAMP NOT NULL,
    status_http INT NOT NULL
);

CREATE INDEX idx_raw_cnpj_chave_data ON raw_cnpj (chave, data_chamada);

CREATE TABLE raw_fipe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chave VARCHAR(255) NOT NULL,
    json_original TEXT NOT NULL,
    data_chamada TIMESTAMP NOT NULL,
    status_http INT NOT NULL
);

CREATE INDEX idx_raw_fipe_chave_data ON raw_fipe (chave, data_chamada);

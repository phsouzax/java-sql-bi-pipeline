-- Adiciona os novos atributos na tabela de empresa individualmente para compatibilidade com o H2
ALTER TABLE empresa ADD COLUMN nome_fantasia VARCHAR(255);
ALTER TABLE empresa ADD COLUMN municipio VARCHAR(100);
ALTER TABLE empresa ADD COLUMN uf VARCHAR(2);
ALTER TABLE empresa ADD COLUMN capital_social DECIMAL(15, 2);
ALTER TABLE empresa ADD COLUMN data_inicio_atividade DATE;

-- Cria a tabela dimensão/associativa para CNAEs secundários (1:N)
CREATE TABLE dim_empresa_cnae (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cnpj_empresa VARCHAR(14) NOT NULL,
    codigo_cnae INT NOT NULL,
    descricao_cnae VARCHAR(255),
    CONSTRAINT fk_empresa_cnae FOREIGN KEY (cnpj_empresa) REFERENCES empresa(cnpj) ON DELETE CASCADE
);
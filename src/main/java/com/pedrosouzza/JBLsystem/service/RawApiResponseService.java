package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.model.RawApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RawApiResponseService {

    private final JdbcTemplate jdbcTemplate;

    public RawApiResponseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long registrarCnpj(String chave, String jsonOriginal, int statusHttp) {
        return registrar("raw_cnpj", chave, jsonOriginal, statusHttp);
    }

    public long registrarFipe(String chave, String jsonOriginal, int statusHttp) {
        return registrar("raw_fipe", chave, jsonOriginal, statusHttp);
    }

    public Optional<RawApiResponse> buscarCnpj(long id) {
        return buscar("raw_cnpj", id);
    }

    public Optional<RawApiResponse> buscarFipe(long id) {
        return buscar("raw_fipe", id);
    }

    private long registrar(String tabela, String chave, String jsonOriginal, int statusHttp) {
        String sql = "INSERT INTO " + tabela
                + " (chave, json_original, data_chamada, status_http) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, chave);
            statement.setString(2, jsonOriginal);
            statement.setObject(3, LocalDateTime.now());
            statement.setInt(4, statusHttp);
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("A resposta bruta foi gravada sem retornar o identificador.");
        }
        return key.longValue();
    }

    private Optional<RawApiResponse> buscar(String tabela, long id) {
        String sql = "SELECT id, chave, json_original, data_chamada, status_http FROM " + tabela + " WHERE id = ?";
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new RawApiResponse(
                resultSet.getLong("id"),
                resultSet.getString("chave"),
                resultSet.getString("json_original"),
                resultSet.getTimestamp("data_chamada").toLocalDateTime(),
                resultSet.getInt("status_http")
        ), id).stream().findFirst();
    }
}

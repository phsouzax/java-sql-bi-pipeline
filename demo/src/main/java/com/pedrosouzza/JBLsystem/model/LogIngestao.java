package com.pedrosouzza.JBLsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_ingestao")
public class LogIngestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_ingestao", nullable = false)
    private String tipoIngestao;

    @Column(nullable = false)
    private String status;

    @Column(name = "mensagem_erro")
    private String mensagemErro;

    @Column(name = "data_execucao", nullable = false)
    private LocalDateTime dataExecucao;

    public LogIngestao() {}

    public LogIngestao(String tipoIngestao, String status, String mensagemErro, LocalDateTime dataExecucao) {
        this.tipoIngestao = tipoIngestao;
        this.status = status;
        this.mensagemErro = mensagemErro;
        this.dataExecucao = dataExecucao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoIngestao() { return tipoIngestao; }
    public void setTipoIngestao(String tipoIngestao) { this.tipoIngestao = tipoIngestao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMensagemErro() { return mensagemErro; }
    public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }

    public LocalDateTime getDataExecucao() { return dataExecucao; }
    public void setDataExecucao(LocalDateTime dataExecucao) { this.dataExecucao = dataExecucao; }
}

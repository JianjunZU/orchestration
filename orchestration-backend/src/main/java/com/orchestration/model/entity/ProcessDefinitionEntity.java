package com.orchestration.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orch_process_definition")
public class ProcessDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String processKey;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String bpmnXml;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false)
    private String status = "DRAFT";

    private String flowableDeploymentId;

    private String flowableProcessDefinitionId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private String createdBy;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBpmnXml() { return bpmnXml; }
    public void setBpmnXml(String bpmnXml) { this.bpmnXml = bpmnXml; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFlowableDeploymentId() { return flowableDeploymentId; }
    public void setFlowableDeploymentId(String flowableDeploymentId) { this.flowableDeploymentId = flowableDeploymentId; }

    public String getFlowableProcessDefinitionId() { return flowableProcessDefinitionId; }
    public void setFlowableProcessDefinitionId(String flowableProcessDefinitionId) { this.flowableProcessDefinitionId = flowableProcessDefinitionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}

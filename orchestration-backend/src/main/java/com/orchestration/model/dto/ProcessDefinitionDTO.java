package com.orchestration.model.dto;

import jakarta.validation.constraints.NotBlank;

public class ProcessDefinitionDTO {

    private Long id;

    @NotBlank(message = "Process key is required")
    private String processKey;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "BPMN XML is required")
    private String bpmnXml;

    private String status;
    private Integer version;
    private String flowableDeploymentId;
    private String flowableProcessDefinitionId;

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getFlowableDeploymentId() { return flowableDeploymentId; }
    public void setFlowableDeploymentId(String flowableDeploymentId) { this.flowableDeploymentId = flowableDeploymentId; }

    public String getFlowableProcessDefinitionId() { return flowableProcessDefinitionId; }
    public void setFlowableProcessDefinitionId(String flowableProcessDefinitionId) { this.flowableProcessDefinitionId = flowableProcessDefinitionId; }
}

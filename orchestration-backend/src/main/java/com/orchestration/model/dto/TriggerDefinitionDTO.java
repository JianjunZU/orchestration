package com.orchestration.model.dto;

import jakarta.validation.constraints.NotBlank;

public class TriggerDefinitionDTO {

    private Long id;

    @NotBlank(message = "Trigger name is required")
    private String name;

    @NotBlank(message = "Trigger type is required")
    private String triggerType;

    @NotBlank(message = "Trigger source is required")
    private String triggerSource;

    @NotBlank(message = "Process key is required")
    private String processKey;

    private String filterExpression;
    private Boolean enabled = true;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }

    public String getTriggerSource() { return triggerSource; }
    public void setTriggerSource(String triggerSource) { this.triggerSource = triggerSource; }

    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }

    public String getFilterExpression() { return filterExpression; }
    public void setFilterExpression(String filterExpression) { this.filterExpression = filterExpression; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}

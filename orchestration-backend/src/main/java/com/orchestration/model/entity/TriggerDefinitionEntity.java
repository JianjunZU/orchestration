package com.orchestration.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Stores trigger configuration: which Kafka topic / signal / message
 * should start or resume a process instance.
 */
@Entity
@Table(name = "orch_trigger_definition")
public class TriggerDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** KAFKA_MESSAGE, SIGNAL, MESSAGE */
    @Column(nullable = false)
    private String triggerType;

    /** For Kafka triggers: the topic name; for signal/message: the signal/message name */
    @Column(nullable = false)
    private String triggerSource;

    /** The process key to trigger */
    @Column(nullable = false)
    private String processKey;

    /** Optional: filter expression to evaluate against incoming message payload */
    @Column(length = 1000)
    private String filterExpression;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

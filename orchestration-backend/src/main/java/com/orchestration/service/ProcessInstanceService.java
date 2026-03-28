package com.orchestration.service;

import com.orchestration.model.dto.ProcessInstanceDTO;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProcessInstanceService {

    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceService.class);

    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public ProcessInstanceService(RuntimeService runtimeService, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    public ProcessInstanceDTO startProcess(String processKey, String businessKey, Map<String, Object> variables) {
        ProcessInstance instance;
        if (businessKey != null) {
            instance = runtimeService.startProcessInstanceByKey(processKey, businessKey, variables);
        } else {
            instance = runtimeService.startProcessInstanceByKey(processKey, variables);
        }

        log.info("Started process instance: {} (key={})", instance.getId(), processKey);

        ProcessInstanceDTO dto = new ProcessInstanceDTO();
        dto.setProcessInstanceId(instance.getId());
        dto.setProcessDefinitionId(instance.getProcessDefinitionId());
        dto.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        dto.setBusinessKey(instance.getBusinessKey());
        dto.setVariables(variables);
        dto.setStatus("RUNNING");
        return dto;
    }

    public List<ProcessInstanceDTO> listRunning(String processKey) {
        var query = runtimeService.createProcessInstanceQuery();
        if (processKey != null) {
            query.processDefinitionKey(processKey);
        }

        return query.list().stream().map(pi -> {
            ProcessInstanceDTO dto = new ProcessInstanceDTO();
            dto.setProcessInstanceId(pi.getId());
            dto.setProcessDefinitionId(pi.getProcessDefinitionId());
            dto.setProcessDefinitionKey(pi.getProcessDefinitionKey());
            dto.setBusinessKey(pi.getBusinessKey());
            dto.setStatus("RUNNING");
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ProcessInstanceDTO> listHistory(String processKey) {
        var query = historyService.createHistoricProcessInstanceQuery();
        if (processKey != null) {
            query.processDefinitionKey(processKey);
        }

        return query.orderByProcessInstanceStartTime().desc().listPage(0, 100)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProcessInstanceDTO getInstanceDetail(String processInstanceId) {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (hpi == null) {
            throw new RuntimeException("Process instance not found: " + processInstanceId);
        }

        ProcessInstanceDTO dto = toDTO(hpi);
        dto.setVariables(runtimeService.getVariables(processInstanceId));
        return dto;
    }

    public void stopProcess(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("Stopped process instance: {} (reason: {})", processInstanceId, reason);
    }

    /**
     * Send a signal event to Flowable (for signal-based triggers).
     */
    public void sendSignal(String signalName, Map<String, Object> variables) {
        runtimeService.signalEventReceived(signalName, variables);
        log.info("Signal sent: {}", signalName);
    }

    /**
     * Send a message event to Flowable (for message-based triggers).
     */
    public ProcessInstanceDTO sendMessage(String messageName, Map<String, Object> variables) {
        ProcessInstance instance = runtimeService.startProcessInstanceByMessage(messageName, variables);
        log.info("Message-started process instance: {} (message={})", instance.getId(), messageName);

        ProcessInstanceDTO dto = new ProcessInstanceDTO();
        dto.setProcessInstanceId(instance.getId());
        dto.setProcessDefinitionId(instance.getProcessDefinitionId());
        dto.setStatus("RUNNING");
        return dto;
    }

    private ProcessInstanceDTO toDTO(HistoricProcessInstance hpi) {
        ProcessInstanceDTO dto = new ProcessInstanceDTO();
        dto.setProcessInstanceId(hpi.getId());
        dto.setProcessDefinitionId(hpi.getProcessDefinitionId());
        dto.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
        dto.setBusinessKey(hpi.getBusinessKey());
        dto.setStartTime(hpi.getStartTime() != null ? hpi.getStartTime().toString() : null);
        dto.setEndTime(hpi.getEndTime() != null ? hpi.getEndTime().toString() : null);
        dto.setStatus(hpi.getEndTime() != null ? "COMPLETED" : "RUNNING");
        return dto;
    }
}

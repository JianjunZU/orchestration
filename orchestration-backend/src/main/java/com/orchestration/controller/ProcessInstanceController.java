package com.orchestration.controller;

import com.orchestration.model.dto.ProcessInstanceDTO;
import com.orchestration.service.ProcessInstanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/process-instances")
public class ProcessInstanceController {

    private final ProcessInstanceService service;

    public ProcessInstanceController(ProcessInstanceService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public ResponseEntity<ProcessInstanceDTO> start(@RequestBody Map<String, Object> request) {
        String processKey = (String) request.get("processKey");
        String businessKey = (String) request.get("businessKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.getOrDefault("variables", Map.of());
        return ResponseEntity.ok(service.startProcess(processKey, businessKey, variables));
    }

    @GetMapping("/running")
    public ResponseEntity<List<ProcessInstanceDTO>> listRunning(
            @RequestParam(required = false) String processKey) {
        return ResponseEntity.ok(service.listRunning(processKey));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ProcessInstanceDTO>> listHistory(
            @RequestParam(required = false) String processKey) {
        return ResponseEntity.ok(service.listHistory(processKey));
    }

    @GetMapping("/{processInstanceId}")
    public ResponseEntity<ProcessInstanceDTO> detail(@PathVariable String processInstanceId) {
        return ResponseEntity.ok(service.getInstanceDetail(processInstanceId));
    }

    @DeleteMapping("/{processInstanceId}")
    public ResponseEntity<Map<String, String>> stop(
            @PathVariable String processInstanceId,
            @RequestParam(defaultValue = "Manual stop") String reason) {
        service.stopProcess(processInstanceId, reason);
        return ResponseEntity.ok(Map.of("message", "Process stopped"));
    }

    @PostMapping("/signal")
    public ResponseEntity<Map<String, String>> sendSignal(@RequestBody Map<String, Object> request) {
        String signalName = (String) request.get("signalName");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.getOrDefault("variables", Map.of());
        service.sendSignal(signalName, variables);
        return ResponseEntity.ok(Map.of("message", "Signal sent: " + signalName));
    }

    @PostMapping("/message")
    public ResponseEntity<ProcessInstanceDTO> sendMessage(@RequestBody Map<String, Object> request) {
        String messageName = (String) request.get("messageName");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.getOrDefault("variables", Map.of());
        return ResponseEntity.ok(service.sendMessage(messageName, variables));
    }
}

package com.orchestration.controller;

import com.orchestration.model.dto.ProcessDefinitionDTO;
import com.orchestration.service.ProcessDefinitionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/process-definitions")
public class ProcessDefinitionController {

    private final ProcessDefinitionService service;

    public ProcessDefinitionController(ProcessDefinitionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProcessDefinitionDTO>> list() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessDefinitionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/key/{processKey}")
    public ResponseEntity<ProcessDefinitionDTO> getByKey(@PathVariable String processKey) {
        return ResponseEntity.ok(service.getByProcessKey(processKey));
    }

    @PostMapping
    public ResponseEntity<ProcessDefinitionDTO> save(@Valid @RequestBody ProcessDefinitionDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<ProcessDefinitionDTO> deploy(@PathVariable Long id) {
        return ResponseEntity.ok(service.deploy(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }
}

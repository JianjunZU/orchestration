package com.orchestration.controller;

import com.orchestration.model.dto.TriggerDefinitionDTO;
import com.orchestration.service.TriggerDefinitionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/triggers")
public class TriggerController {

    private final TriggerDefinitionService service;

    public TriggerController(TriggerDefinitionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TriggerDefinitionDTO>> list() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TriggerDefinitionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<TriggerDefinitionDTO> save(@Valid @RequestBody TriggerDefinitionDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<TriggerDefinitionDTO> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleEnabled(id));
    }
}

package com.orchestration.controller;

import com.orchestration.engine.executor.ExecutorHandler;
import com.orchestration.engine.executor.ExecutorHandlerRegistry;
import com.orchestration.engine.logic.LogicHandler;
import com.orchestration.engine.logic.LogicHandlerRegistry;
import com.orchestration.engine.trigger.TriggerHandler;
import com.orchestration.engine.trigger.TriggerHandlerRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides the frontend with metadata about available extensible element types.
 * Used by the bpmn.js palette to populate custom element options.
 */
@RestController
@RequestMapping("/api/registry")
public class ElementRegistryController {

    @GetMapping("/executor-types")
    public ResponseEntity<List<Map<String, String>>> getExecutorTypes() {
        return ResponseEntity.ok(
                ExecutorHandlerRegistry.getAllHandlers().values().stream()
                        .map(h -> Map.of("type", h.getType(), "name", h.getType() + " Executor"))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/logic-types")
    public ResponseEntity<List<Map<String, String>>> getLogicTypes() {
        return ResponseEntity.ok(
                LogicHandlerRegistry.getAllHandlers().values().stream()
                        .map(h -> Map.of("type", h.getType(), "name", h.getType() + " Logic"))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/trigger-types")
    public ResponseEntity<List<Map<String, String>>> getTriggerTypes() {
        return ResponseEntity.ok(List.of(
                Map.of("type", "KAFKA_MESSAGE", "name", "Kafka Message Trigger"),
                Map.of("type", "SIGNAL", "name", "Signal Trigger"),
                Map.of("type", "MESSAGE", "name", "Message Trigger")
        ));
    }
}

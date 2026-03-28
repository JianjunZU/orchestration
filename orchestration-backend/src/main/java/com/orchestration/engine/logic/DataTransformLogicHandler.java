package com.orchestration.engine.logic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * Transforms/maps variables from one form to another.
 * Config: {"mappings": {"sourceVar": "targetVar", ...}, "constants": {"key": "value"}}
 */
@Component
public class DataTransformLogicHandler implements LogicHandler {

    private static final Logger log = LoggerFactory.getLogger(DataTransformLogicHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getType() {
        return "DATA_TRANSFORM";
    }

    @Override
    public void execute(DelegateExecution execution, String config) throws Exception {
        JsonNode configNode = objectMapper.readTree(config);

        // Variable mappings
        if (configNode.has("mappings")) {
            JsonNode mappings = configNode.get("mappings");
            Iterator<Map.Entry<String, JsonNode>> fields = mappings.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String sourceVar = entry.getKey();
                String targetVar = entry.getValue().asText();
                Object value = execution.getVariable(sourceVar);
                execution.setVariable(targetVar, value);
                log.info("[DataTransform] Mapped {} -> {} = {}", sourceVar, targetVar, value);
            }
        }

        // Constants
        if (configNode.has("constants")) {
            JsonNode constants = configNode.get("constants");
            Iterator<Map.Entry<String, JsonNode>> fields = constants.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                execution.setVariable(entry.getKey(), entry.getValue().asText());
                log.info("[DataTransform] Set constant {} = {}", entry.getKey(), entry.getValue().asText());
            }
        }
    }
}

package com.orchestration.engine.trigger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles Kafka message triggers. Parses message payload into process variables.
 */
@Component
public class KafkaTriggerHandler implements TriggerHandler {

    private static final Logger log = LoggerFactory.getLogger(KafkaTriggerHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getType() {
        return "KAFKA_MESSAGE";
    }

    @Override
    public Map<String, Object> processTrigger(String triggerSource, String payload) throws Exception {
        log.info("[KafkaTrigger] Processing message from topic: {}", triggerSource);

        Map<String, Object> variables = new HashMap<>();
        variables.put("triggerType", "KAFKA_MESSAGE");
        variables.put("triggerSource", triggerSource);
        variables.put("triggerTimestamp", System.currentTimeMillis());

        // Parse payload as JSON map if possible
        try {
            Map<String, Object> payloadMap = objectMapper.readValue(payload,
                    new TypeReference<Map<String, Object>>() {});
            variables.put("triggerPayload", payloadMap);
            variables.putAll(payloadMap);
        } catch (Exception e) {
            // If not valid JSON, store as string
            variables.put("triggerPayload", payload);
        }

        return variables;
    }
}

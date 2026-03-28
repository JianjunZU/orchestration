package com.orchestration.engine.trigger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles signal-based triggers via Flowable signal events.
 */
@Component
public class SignalTriggerHandler implements TriggerHandler {

    private static final Logger log = LoggerFactory.getLogger(SignalTriggerHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getType() {
        return "SIGNAL";
    }

    @Override
    public Map<String, Object> processTrigger(String triggerSource, String payload) throws Exception {
        log.info("[SignalTrigger] Processing signal: {}", triggerSource);

        Map<String, Object> variables = new HashMap<>();
        variables.put("triggerType", "SIGNAL");
        variables.put("signalName", triggerSource);
        variables.put("triggerTimestamp", System.currentTimeMillis());

        if (payload != null && !payload.isBlank()) {
            try {
                Map<String, Object> payloadMap = objectMapper.readValue(payload,
                        new TypeReference<Map<String, Object>>() {});
                variables.putAll(payloadMap);
            } catch (Exception e) {
                variables.put("triggerPayload", payload);
            }
        }

        return variables;
    }
}

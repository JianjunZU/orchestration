package com.orchestration.engine.trigger;

import java.util.Map;

/**
 * Extension point for custom trigger types.
 */
public interface TriggerHandler {

    String getType();

    /**
     * Process an incoming trigger event.
     *
     * @param triggerSource the source identifier (topic, signal name, etc.)
     * @param payload       the event payload
     * @return map of variables to pass to the started process instance
     */
    Map<String, Object> processTrigger(String triggerSource, String payload) throws Exception;
}

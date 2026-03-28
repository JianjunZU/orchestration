package com.orchestration.engine.executor;

import org.flowable.engine.delegate.DelegateExecution;

/**
 * Extension point for custom executor types.
 * Implement this interface and register via ExecutorHandlerRegistry.
 */
public interface ExecutorHandler {

    /**
     * @return unique type identifier, e.g. "HTTP", "SCRIPT", "SHELL"
     */
    String getType();

    /**
     * Execute the task logic.
     *
     * @param execution  the Flowable delegate execution context
     * @param config     JSON config string from the BPMN extension element
     * @return execution result (stored as process variable)
     */
    Object execute(DelegateExecution execution, String config) throws Exception;
}

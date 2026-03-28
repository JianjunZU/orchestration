package com.orchestration.engine.logic;

import org.flowable.engine.delegate.DelegateExecution;

/**
 * Extension point for custom logic controller types.
 * Implement this interface and register via LogicHandlerRegistry.
 */
public interface LogicHandler {

    String getType();

    void execute(DelegateExecution execution, String config) throws Exception;
}

package com.orchestration.engine.executor;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base executor delegate for ServiceTask nodes in the BPMN process.
 * Each executor ServiceTask specifies an executorType via a field expression.
 * The delegate dispatches to the appropriate ExecutorHandler at runtime.
 *
 * BPMN XML usage:
 * <serviceTask id="exec1" name="HTTP Executor"
 *   flowable:delegateExpression="${executorDelegate}">
 *   <extensionElements>
 *     <flowable:field name="executorType" stringValue="HTTP" />
 *     <flowable:field name="executorConfig" stringValue='{"url":"...","method":"POST"}' />
 *   </extensionElements>
 * </serviceTask>
 */
public class ExecutorServiceTaskDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(ExecutorServiceTaskDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String executorType = getFieldValue(execution, "executorType");
        String executorConfig = getFieldValue(execution, "executorConfig");

        log.info("[Executor] Executing task '{}' | type={} | processInstanceId={}",
                execution.getCurrentActivityId(), executorType, execution.getProcessInstanceId());

        ExecutorHandler handler = ExecutorHandlerRegistry.getHandler(executorType);
        if (handler == null) {
            throw new RuntimeException("Unknown executor type: " + executorType);
        }

        try {
            Object result = handler.execute(execution, executorConfig);
            execution.setVariable(execution.getCurrentActivityId() + "_result", result);
            execution.setVariable(execution.getCurrentActivityId() + "_status", "SUCCESS");
            log.info("[Executor] Task '{}' completed successfully", execution.getCurrentActivityId());
        } catch (Exception e) {
            execution.setVariable(execution.getCurrentActivityId() + "_status", "FAILED");
            execution.setVariable(execution.getCurrentActivityId() + "_error", e.getMessage());
            log.error("[Executor] Task '{}' failed: {}", execution.getCurrentActivityId(), e.getMessage(), e);
            throw new RuntimeException("Executor failed: " + e.getMessage(), e);
        }
    }

    private String getFieldValue(DelegateExecution execution, String fieldName) {
        Object value = execution.getVariable(fieldName);
        if (value == null) {
            // Try extension element fields stored during deployment
            value = execution.getVariable(execution.getCurrentActivityId() + "_" + fieldName);
        }
        return value != null ? value.toString() : "";
    }
}

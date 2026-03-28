package com.orchestration.engine.logic;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logic controller delegate for ServiceTask nodes that perform branching,
 * data transformation, variable manipulation, or conditional evaluation.
 *
 * BPMN XML usage:
 * <serviceTask id="logic1" name="Condition Check"
 *   flowable:delegateExpression="${logicControllerDelegate}">
 *   <extensionElements>
 *     <flowable:field name="logicType" stringValue="CONDITION" />
 *     <flowable:field name="logicConfig" stringValue='{"expression":"${amount > 1000}","trueVar":"approved","falseVar":"rejected"}' />
 *   </extensionElements>
 * </serviceTask>
 */
public class LogicControllerDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(LogicControllerDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String logicType = getFieldValue(execution, "logicType");
        String logicConfig = getFieldValue(execution, "logicConfig");

        log.info("[LogicController] Executing logic '{}' | type={} | processInstanceId={}",
                execution.getCurrentActivityId(), logicType, execution.getProcessInstanceId());

        LogicHandler handler = LogicHandlerRegistry.getHandler(logicType);
        if (handler == null) {
            throw new RuntimeException("Unknown logic controller type: " + logicType);
        }

        try {
            handler.execute(execution, logicConfig);
            log.info("[LogicController] Logic '{}' executed successfully", execution.getCurrentActivityId());
        } catch (Exception e) {
            log.error("[LogicController] Logic '{}' failed: {}", execution.getCurrentActivityId(), e.getMessage(), e);
            throw new RuntimeException("Logic controller failed: " + e.getMessage(), e);
        }
    }

    private String getFieldValue(DelegateExecution execution, String fieldName) {
        Object value = execution.getVariable(fieldName);
        if (value == null) {
            value = execution.getVariable(execution.getCurrentActivityId() + "_" + fieldName);
        }
        return value != null ? value.toString() : "";
    }
}

package com.orchestration.engine.logic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Evaluates a condition expression and sets a result variable.
 * Config: {"expression": "${amount > 1000}", "resultVariable": "checkResult"}
 *
 * The expression is evaluated against process variables using simple comparison.
 */
@Component
public class ConditionLogicHandler implements LogicHandler {

    private static final Logger log = LoggerFactory.getLogger(ConditionLogicHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getType() {
        return "CONDITION";
    }

    @Override
    public void execute(DelegateExecution execution, String config) throws Exception {
        JsonNode configNode = objectMapper.readTree(config);
        String expression = configNode.path("expression").asText();
        String resultVariable = configNode.path("resultVariable").asText("conditionResult");

        log.info("[ConditionLogic] Evaluating expression: {}", expression);

        // Simple expression evaluation: supports variable comparison
        boolean result = evaluateExpression(execution, expression);
        execution.setVariable(resultVariable, result);

        log.info("[ConditionLogic] Result: {} = {}", resultVariable, result);
    }

    private boolean evaluateExpression(DelegateExecution execution, String expression) {
        // Strip ${ and } if present
        String expr = expression.trim();
        if (expr.startsWith("${") && expr.endsWith("}")) {
            expr = expr.substring(2, expr.length() - 1).trim();
        }

        // Support operators: >, <, >=, <=, ==, !=
        String[] operators = {">=", "<=", "!=", "==", ">", "<"};
        for (String op : operators) {
            int idx = expr.indexOf(op);
            if (idx > 0) {
                String leftExpr = expr.substring(0, idx).trim();
                String rightExpr = expr.substring(idx + op.length()).trim();

                Object leftVal = resolveValue(execution, leftExpr);
                Object rightVal = resolveValue(execution, rightExpr);

                return compare(leftVal, rightVal, op);
            }
        }

        // Treat as boolean variable
        Object val = execution.getVariable(expr);
        return val instanceof Boolean ? (Boolean) val : Boolean.parseBoolean(String.valueOf(val));
    }

    private Object resolveValue(DelegateExecution execution, String expr) {
        // Try as variable name
        Object val = execution.getVariable(expr);
        if (val != null) return val;

        // Try as number
        try { return Double.parseDouble(expr); } catch (NumberFormatException ignored) {}

        // Try as quoted string
        if (expr.startsWith("'") && expr.endsWith("'")) {
            return expr.substring(1, expr.length() - 1);
        }

        return expr;
    }

    private boolean compare(Object left, Object right, String op) {
        if (left instanceof Number l && right instanceof Number r) {
            double ld = l.doubleValue();
            double rd = r.doubleValue();
            return switch (op) {
                case ">" -> ld > rd;
                case "<" -> ld < rd;
                case ">=" -> ld >= rd;
                case "<=" -> ld <= rd;
                case "==" -> ld == rd;
                case "!=" -> ld != rd;
                default -> false;
            };
        }
        String ls = String.valueOf(left);
        String rs = String.valueOf(right);
        return switch (op) {
            case "==", ">=" , "<=" -> ls.equals(rs);
            case "!=" -> !ls.equals(rs);
            default -> false;
        };
    }
}

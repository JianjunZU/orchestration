package com.orchestration.engine.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.Map;

/**
 * Built-in executor: runs scripts (JavaScript/Groovy).
 * Config JSON: {"language": "javascript", "script": "var x = 1 + 1; x;"}
 */
@Component
public class ScriptExecutorHandler implements ExecutorHandler {

    private static final Logger log = LoggerFactory.getLogger(ScriptExecutorHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getType() {
        return "SCRIPT";
    }

    @Override
    public Object execute(DelegateExecution execution, String config) throws Exception {
        JsonNode configNode = objectMapper.readTree(config);
        String language = configNode.path("language").asText("javascript");
        String script = configNode.path("script").asText();

        log.info("[ScriptExecutor] Running {} script in process {}", language, execution.getProcessInstanceId());

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(language);
        if (engine == null) {
            throw new RuntimeException("Script engine not found for language: " + language);
        }

        SimpleBindings bindings = new SimpleBindings();
        bindings.put("execution", execution);
        bindings.put("variables", execution.getVariables());

        return engine.eval(script, bindings);
    }
}

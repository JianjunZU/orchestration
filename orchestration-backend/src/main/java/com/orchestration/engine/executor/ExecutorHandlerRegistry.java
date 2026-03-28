package com.orchestration.engine.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for executor handlers. Automatically discovers and registers
 * all ExecutorHandler beans at startup. New executor types can be added
 * by simply implementing ExecutorHandler and annotating with @Component.
 */
@Component
public class ExecutorHandlerRegistry {

    private static final Logger log = LoggerFactory.getLogger(ExecutorHandlerRegistry.class);
    private static final Map<String, ExecutorHandler> HANDLERS = new ConcurrentHashMap<>();

    private final List<ExecutorHandler> handlerBeans;

    public ExecutorHandlerRegistry(List<ExecutorHandler> handlerBeans) {
        this.handlerBeans = handlerBeans;
    }

    @PostConstruct
    public void init() {
        for (ExecutorHandler handler : handlerBeans) {
            HANDLERS.put(handler.getType().toUpperCase(), handler);
            log.info("[ExecutorRegistry] Registered executor type: {}", handler.getType());
        }
    }

    public static ExecutorHandler getHandler(String type) {
        return HANDLERS.get(type != null ? type.toUpperCase() : "");
    }

    public static void register(ExecutorHandler handler) {
        HANDLERS.put(handler.getType().toUpperCase(), handler);
    }

    public static Map<String, ExecutorHandler> getAllHandlers() {
        return Map.copyOf(HANDLERS);
    }
}

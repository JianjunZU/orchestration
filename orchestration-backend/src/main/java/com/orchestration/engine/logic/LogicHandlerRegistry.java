package com.orchestration.engine.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LogicHandlerRegistry {

    private static final Logger log = LoggerFactory.getLogger(LogicHandlerRegistry.class);
    private static final Map<String, LogicHandler> HANDLERS = new ConcurrentHashMap<>();

    private final List<LogicHandler> handlerBeans;

    public LogicHandlerRegistry(List<LogicHandler> handlerBeans) {
        this.handlerBeans = handlerBeans;
    }

    @PostConstruct
    public void init() {
        for (LogicHandler handler : handlerBeans) {
            HANDLERS.put(handler.getType().toUpperCase(), handler);
            log.info("[LogicRegistry] Registered logic type: {}", handler.getType());
        }
    }

    public static LogicHandler getHandler(String type) {
        return HANDLERS.get(type != null ? type.toUpperCase() : "");
    }

    public static void register(LogicHandler handler) {
        HANDLERS.put(handler.getType().toUpperCase(), handler);
    }

    public static Map<String, LogicHandler> getAllHandlers() {
        return Map.copyOf(HANDLERS);
    }
}

package com.orchestration.engine.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TriggerHandlerRegistry {

    private static final Logger log = LoggerFactory.getLogger(TriggerHandlerRegistry.class);
    private static final Map<String, TriggerHandler> HANDLERS = new ConcurrentHashMap<>();

    private final List<TriggerHandler> handlerBeans;

    public TriggerHandlerRegistry(List<TriggerHandler> handlerBeans) {
        this.handlerBeans = handlerBeans;
    }

    @PostConstruct
    public void init() {
        for (TriggerHandler handler : handlerBeans) {
            HANDLERS.put(handler.getType().toUpperCase(), handler);
            log.info("[TriggerRegistry] Registered trigger type: {}", handler.getType());
        }
    }

    public static TriggerHandler getHandler(String type) {
        return HANDLERS.get(type != null ? type.toUpperCase() : "");
    }

    public static void register(TriggerHandler handler) {
        HANDLERS.put(handler.getType().toUpperCase(), handler);
    }
}

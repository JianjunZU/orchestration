package com.orchestration.engine.listener;

import com.orchestration.engine.trigger.TriggerHandler;
import com.orchestration.engine.trigger.TriggerHandlerRegistry;
import com.orchestration.model.entity.TriggerDefinitionEntity;
import com.orchestration.repository.TriggerDefinitionRepository;
import com.orchestration.service.DistributedLockService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.flowable.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Listens to Kafka topics configured as triggers.
 * Uses distributed lock to ensure only one instance processes each message
 * in a dual-instance setup.
 */
@Component
public class KafkaTriggerListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaTriggerListener.class);

    private final TriggerDefinitionRepository triggerDefinitionRepository;
    private final RuntimeService runtimeService;
    private final DistributedLockService lockService;

    public KafkaTriggerListener(TriggerDefinitionRepository triggerDefinitionRepository,
                                 RuntimeService runtimeService,
                                 DistributedLockService lockService) {
        this.triggerDefinitionRepository = triggerDefinitionRepository;
        this.runtimeService = runtimeService;
        this.lockService = lockService;
    }

    @KafkaListener(topics = "orchestration-trigger", groupId = "orchestration-trigger-group")
    public void onTriggerMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String topic = record.topic();
        String payload = record.value();
        String lockKey = "trigger:" + topic + ":" + record.partition() + ":" + record.offset();

        log.info("[KafkaTrigger] Received message on topic={}, partition={}, offset={}",
                topic, record.partition(), record.offset());

        // Distributed lock ensures only one instance processes this trigger
        boolean acquired = lockService.tryLock(lockKey, 30000);
        if (!acquired) {
            log.info("[KafkaTrigger] Lock not acquired, skipping (processed by other instance)");
            ack.acknowledge();
            return;
        }

        try {
            processTrigger(topic, payload);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[KafkaTrigger] Error processing trigger: {}", e.getMessage(), e);
            ack.acknowledge(); // Still acknowledge to avoid infinite retry
        } finally {
            lockService.unlock(lockKey);
        }
    }

    @KafkaListener(topics = "orchestration-event", groupId = "orchestration-event-group")
    public void onEventMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        onTriggerMessage(record, ack);
    }

    private void processTrigger(String topic, String payload) {
        List<TriggerDefinitionEntity> triggers = triggerDefinitionRepository
                .findByTriggerSourceAndTriggerTypeAndEnabled(topic, "KAFKA_MESSAGE", true);

        if (triggers.isEmpty()) {
            log.warn("[KafkaTrigger] No trigger definitions found for topic: {}", topic);
            return;
        }

        TriggerHandler handler = TriggerHandlerRegistry.getHandler("KAFKA_MESSAGE");
        if (handler == null) {
            log.error("[KafkaTrigger] No handler registered for KAFKA_MESSAGE type");
            return;
        }

        for (TriggerDefinitionEntity trigger : triggers) {
            try {
                Map<String, Object> variables = handler.processTrigger(topic, payload);
                variables.put("triggerId", trigger.getId());
                variables.put("triggerName", trigger.getName());

                // Start process using message event or direct start
                runtimeService.startProcessInstanceByKey(trigger.getProcessKey(), variables);
                log.info("[KafkaTrigger] Started process '{}' for trigger '{}'",
                        trigger.getProcessKey(), trigger.getName());
            } catch (Exception e) {
                log.error("[KafkaTrigger] Failed to start process for trigger '{}': {}",
                        trigger.getName(), e.getMessage(), e);
            }
        }
    }
}

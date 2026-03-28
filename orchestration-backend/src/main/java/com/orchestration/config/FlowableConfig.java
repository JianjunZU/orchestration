package com.orchestration.config;

import com.orchestration.engine.executor.ExecutorServiceTaskDelegate;
import com.orchestration.engine.logic.LogicControllerDelegate;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flowable engine configuration.
 * Registers custom service task delegates and configures async executor for dual-instance.
 */
@Configuration
public class FlowableConfig {

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> engineConfigurer() {
        return configuration -> {
            // Enable async executor for dual-instance distributed execution
            configuration.setAsyncExecutorActivate(true);
            // Use shared lock mechanism for dual-instance coordination
            configuration.setAsyncExecutorNumberOfRetries(3);
            configuration.setAsyncExecutorDefaultAsyncJobAcquireWaitTime(5000);
            configuration.setAsyncExecutorDefaultTimerJobAcquireWaitTime(5000);
        };
    }

    @Bean("executorDelegate")
    public ExecutorServiceTaskDelegate executorServiceTaskDelegate() {
        return new ExecutorServiceTaskDelegate();
    }

    @Bean("logicControllerDelegate")
    public LogicControllerDelegate logicControllerDelegate() {
        return new LogicControllerDelegate();
    }
}

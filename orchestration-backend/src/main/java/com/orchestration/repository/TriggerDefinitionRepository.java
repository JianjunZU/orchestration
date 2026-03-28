package com.orchestration.repository;

import com.orchestration.model.entity.TriggerDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriggerDefinitionRepository extends JpaRepository<TriggerDefinitionEntity, Long> {

    List<TriggerDefinitionEntity> findByTriggerTypeAndEnabled(String triggerType, Boolean enabled);

    List<TriggerDefinitionEntity> findByProcessKeyAndEnabled(String processKey, Boolean enabled);

    List<TriggerDefinitionEntity> findByTriggerSourceAndTriggerTypeAndEnabled(
            String triggerSource, String triggerType, Boolean enabled);
}

package com.orchestration.repository;

import com.orchestration.model.entity.ProcessDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinitionEntity, Long> {

    Optional<ProcessDefinitionEntity> findByProcessKey(String processKey);

    Optional<ProcessDefinitionEntity> findByFlowableProcessDefinitionId(String flowableProcessDefinitionId);

    boolean existsByProcessKey(String processKey);
}

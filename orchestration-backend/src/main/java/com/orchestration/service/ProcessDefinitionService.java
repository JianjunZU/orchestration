package com.orchestration.service;

import com.orchestration.model.dto.ProcessDefinitionDTO;
import com.orchestration.model.entity.ProcessDefinitionEntity;
import com.orchestration.repository.ProcessDefinitionRepository;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionService.class);

    private final ProcessDefinitionRepository repository;
    private final RepositoryService repositoryService;

    public ProcessDefinitionService(ProcessDefinitionRepository repository,
                                     RepositoryService repositoryService) {
        this.repository = repository;
        this.repositoryService = repositoryService;
    }

    public List<ProcessDefinitionDTO> listAll() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProcessDefinitionDTO getById(Long id) {
        return repository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Process definition not found: " + id));
    }

    public ProcessDefinitionDTO getByProcessKey(String processKey) {
        return repository.findByProcessKey(processKey).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Process definition not found: " + processKey));
    }

    @Transactional
    public ProcessDefinitionDTO save(ProcessDefinitionDTO dto) {
        ProcessDefinitionEntity entity;

        if (dto.getId() != null) {
            entity = repository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Not found: " + dto.getId()));
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setBpmnXml(dto.getBpmnXml());
            entity.setVersion(entity.getVersion() + 1);
        } else {
            if (repository.existsByProcessKey(dto.getProcessKey())) {
                throw new RuntimeException("Process key already exists: " + dto.getProcessKey());
            }
            entity = new ProcessDefinitionEntity();
            entity.setProcessKey(dto.getProcessKey());
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setBpmnXml(dto.getBpmnXml());
        }

        entity = repository.save(entity);
        log.info("Saved process definition: {} (v{})", entity.getProcessKey(), entity.getVersion());
        return toDTO(entity);
    }

    @Transactional
    public ProcessDefinitionDTO deploy(Long id) {
        ProcessDefinitionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found: " + id));

        Deployment deployment = repositoryService.createDeployment()
                .name(entity.getName())
                .addString(entity.getProcessKey() + ".bpmn20.xml", entity.getBpmnXml())
                .deploy();

        ProcessDefinition processDef = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        entity.setFlowableDeploymentId(deployment.getId());
        entity.setFlowableProcessDefinitionId(processDef.getId());
        entity.setStatus("DEPLOYED");
        entity = repository.save(entity);

        log.info("Deployed process: {} -> flowable id: {}", entity.getProcessKey(), processDef.getId());
        return toDTO(entity);
    }

    @Transactional
    public void delete(Long id) {
        ProcessDefinitionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found: " + id));

        if (entity.getFlowableDeploymentId() != null) {
            repositoryService.deleteDeployment(entity.getFlowableDeploymentId(), true);
        }

        repository.delete(entity);
        log.info("Deleted process definition: {}", entity.getProcessKey());
    }

    private ProcessDefinitionDTO toDTO(ProcessDefinitionEntity entity) {
        ProcessDefinitionDTO dto = new ProcessDefinitionDTO();
        dto.setId(entity.getId());
        dto.setProcessKey(entity.getProcessKey());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setBpmnXml(entity.getBpmnXml());
        dto.setVersion(entity.getVersion());
        dto.setStatus(entity.getStatus());
        dto.setFlowableDeploymentId(entity.getFlowableDeploymentId());
        dto.setFlowableProcessDefinitionId(entity.getFlowableProcessDefinitionId());
        return dto;
    }
}

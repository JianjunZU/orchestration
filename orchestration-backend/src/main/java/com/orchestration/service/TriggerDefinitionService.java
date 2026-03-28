package com.orchestration.service;

import com.orchestration.model.dto.TriggerDefinitionDTO;
import com.orchestration.model.entity.TriggerDefinitionEntity;
import com.orchestration.repository.TriggerDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TriggerDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(TriggerDefinitionService.class);

    private final TriggerDefinitionRepository repository;

    public TriggerDefinitionService(TriggerDefinitionRepository repository) {
        this.repository = repository;
    }

    public List<TriggerDefinitionDTO> listAll() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TriggerDefinitionDTO getById(Long id) {
        return repository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Trigger not found: " + id));
    }

    @Transactional
    public TriggerDefinitionDTO save(TriggerDefinitionDTO dto) {
        TriggerDefinitionEntity entity;

        if (dto.getId() != null) {
            entity = repository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Not found: " + dto.getId()));
        } else {
            entity = new TriggerDefinitionEntity();
        }

        entity.setName(dto.getName());
        entity.setTriggerType(dto.getTriggerType());
        entity.setTriggerSource(dto.getTriggerSource());
        entity.setProcessKey(dto.getProcessKey());
        entity.setFilterExpression(dto.getFilterExpression());
        entity.setEnabled(dto.getEnabled());

        entity = repository.save(entity);
        log.info("Saved trigger: {} (type={}, source={})", entity.getName(),
                entity.getTriggerType(), entity.getTriggerSource());
        return toDTO(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
        log.info("Deleted trigger: {}", id);
    }

    @Transactional
    public TriggerDefinitionDTO toggleEnabled(Long id) {
        TriggerDefinitionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found: " + id));
        entity.setEnabled(!entity.getEnabled());
        entity = repository.save(entity);
        log.info("Trigger {} is now {}", entity.getName(), entity.getEnabled() ? "enabled" : "disabled");
        return toDTO(entity);
    }

    private TriggerDefinitionDTO toDTO(TriggerDefinitionEntity entity) {
        TriggerDefinitionDTO dto = new TriggerDefinitionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setTriggerType(entity.getTriggerType());
        dto.setTriggerSource(entity.getTriggerSource());
        dto.setProcessKey(entity.getProcessKey());
        dto.setFilterExpression(entity.getFilterExpression());
        dto.setEnabled(entity.getEnabled());
        return dto;
    }
}

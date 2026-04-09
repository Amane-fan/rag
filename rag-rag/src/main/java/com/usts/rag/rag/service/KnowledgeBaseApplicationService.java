package com.usts.rag.rag.service;

import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;
import com.usts.rag.common.util.IdGenerator;
import com.usts.rag.domain.entity.KnowledgeBaseEntity;
import com.usts.rag.domain.enums.KnowledgeBaseStatus;
import com.usts.rag.domain.repository.KnowledgeBaseRepository;
import com.usts.rag.rag.config.RagPipelineProperties;
import com.usts.rag.rag.model.CreateKnowledgeBaseCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库应用服务。
 */
@Service
public class KnowledgeBaseApplicationService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final RagPipelineProperties ragPipelineProperties;

    public KnowledgeBaseApplicationService(KnowledgeBaseRepository knowledgeBaseRepository,
                                           RagPipelineProperties ragPipelineProperties) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.ragPipelineProperties = ragPipelineProperties;
    }

    @Transactional
    public KnowledgeBaseEntity create(CreateKnowledgeBaseCommand command) {
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setId(IdGenerator.nextId());
        entity.setName(command.name());
        entity.setDescription(command.description());
        entity.setEmbeddingModel(command.embeddingModel());
        entity.setTopK(command.topK() != null ? command.topK() : ragPipelineProperties.getDefaultTopK());
        entity.setStatus(KnowledgeBaseStatus.ACTIVE.name());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseRepository.save(entity);
        return entity;
    }

    public List<KnowledgeBaseEntity> list() {
        return knowledgeBaseRepository.findAll();
    }

    public KnowledgeBaseEntity get(String id) {
        return knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Knowledge base not found"));
    }

    @Transactional
    public KnowledgeBaseEntity changeStatus(String id, KnowledgeBaseStatus status) {
        KnowledgeBaseEntity entity = get(id);
        entity.setStatus(status.name());
        entity.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseRepository.update(entity);
        return entity;
    }
}

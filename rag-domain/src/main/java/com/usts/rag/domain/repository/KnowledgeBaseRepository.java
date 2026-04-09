package com.usts.rag.domain.repository;

import com.usts.rag.domain.entity.KnowledgeBaseEntity;

import java.util.List;
import java.util.Optional;

public interface KnowledgeBaseRepository {

    void save(KnowledgeBaseEntity knowledgeBase);

    void update(KnowledgeBaseEntity knowledgeBase);

    Optional<KnowledgeBaseEntity> findById(String id);

    List<KnowledgeBaseEntity> findAll();
}

package com.usts.rag.domain.repository;

import com.usts.rag.domain.entity.DocumentRecordEntity;
import com.usts.rag.domain.entity.DocumentSegmentEntity;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository {

    void save(DocumentRecordEntity document);

    void update(DocumentRecordEntity document);

    Optional<DocumentRecordEntity> findById(String id);

    List<DocumentRecordEntity> findByKnowledgeBaseId(String knowledgeBaseId);

    void replaceSegments(String documentId, List<DocumentSegmentEntity> segments);

    List<DocumentSegmentEntity> findSegmentsByKnowledgeBaseId(String knowledgeBaseId, int limit);
}

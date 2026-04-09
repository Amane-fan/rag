package com.usts.rag.infra.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.usts.rag.domain.entity.DocumentRecordEntity;
import com.usts.rag.domain.entity.DocumentSegmentEntity;
import com.usts.rag.domain.repository.DocumentRepository;
import com.usts.rag.infra.persistence.mapper.DocumentRecordMapper;
import com.usts.rag.infra.persistence.mapper.DocumentSegmentMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisDocumentRepository implements DocumentRepository {

    private final DocumentRecordMapper documentRecordMapper;
    private final DocumentSegmentMapper documentSegmentMapper;

    public MybatisDocumentRepository(DocumentRecordMapper documentRecordMapper,
                                     DocumentSegmentMapper documentSegmentMapper) {
        this.documentRecordMapper = documentRecordMapper;
        this.documentSegmentMapper = documentSegmentMapper;
    }

    @Override
    public void save(DocumentRecordEntity document) {
        documentRecordMapper.insert(document);
    }

    @Override
    public void update(DocumentRecordEntity document) {
        documentRecordMapper.updateById(document);
    }

    @Override
    public Optional<DocumentRecordEntity> findById(String id) {
        return Optional.ofNullable(documentRecordMapper.selectById(id));
    }

    @Override
    public List<DocumentRecordEntity> findByKnowledgeBaseId(String knowledgeBaseId) {
        return documentRecordMapper.selectList(new LambdaQueryWrapper<DocumentRecordEntity>()
                .eq(DocumentRecordEntity::getKnowledgeBaseId, knowledgeBaseId)
                .orderByDesc(DocumentRecordEntity::getCreatedAt));
    }

    @Override
    @Transactional
    public void replaceSegments(String documentId, List<DocumentSegmentEntity> segments) {
        documentSegmentMapper.delete(new LambdaUpdateWrapper<DocumentSegmentEntity>()
                .eq(DocumentSegmentEntity::getDocumentId, documentId));
        for (DocumentSegmentEntity segment : segments) {
            documentSegmentMapper.insert(segment);
        }
    }

    @Override
    public List<DocumentSegmentEntity> findSegmentsByKnowledgeBaseId(String knowledgeBaseId, int limit) {
        return documentSegmentMapper.selectList(new LambdaQueryWrapper<DocumentSegmentEntity>()
                .eq(DocumentSegmentEntity::getKnowledgeBaseId, knowledgeBaseId)
                .orderByDesc(DocumentSegmentEntity::getCreatedAt)
                .last("limit " + limit));
    }
}

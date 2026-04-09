package com.usts.rag.rag.service;

import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;
import com.usts.rag.common.util.IdGenerator;
import com.usts.rag.domain.entity.AsyncTaskEntity;
import com.usts.rag.domain.entity.DocumentRecordEntity;
import com.usts.rag.domain.entity.DocumentSegmentEntity;
import com.usts.rag.domain.enums.DocumentStatus;
import com.usts.rag.domain.enums.TaskStatus;
import com.usts.rag.domain.port.VectorKnowledgeStore;
import com.usts.rag.domain.repository.AsyncTaskRepository;
import com.usts.rag.domain.repository.DocumentRepository;
import com.usts.rag.rag.config.RagPipelineProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档索引服务。
 * <p>
 * 消费异步任务后执行切片、落库与向量索引，维护文档和任务状态流转。
 */
@Service
public class DocumentIndexingService {

    private final AsyncTaskRepository asyncTaskRepository;
    private final DocumentRepository documentRepository;
    private final VectorKnowledgeStore vectorKnowledgeStore;
    private final RagPipelineProperties ragPipelineProperties;

    public DocumentIndexingService(AsyncTaskRepository asyncTaskRepository,
                                   DocumentRepository documentRepository,
                                   VectorKnowledgeStore vectorKnowledgeStore,
                                   RagPipelineProperties ragPipelineProperties) {
        this.asyncTaskRepository = asyncTaskRepository;
        this.documentRepository = documentRepository;
        this.vectorKnowledgeStore = vectorKnowledgeStore;
        this.ragPipelineProperties = ragPipelineProperties;
    }

    @Transactional
    public void process(String taskId, String documentId) {
        AsyncTaskEntity task = asyncTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Task not found"));
        DocumentRecordEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Document not found"));

        LocalDateTime now = LocalDateTime.now();
        task.setStatus(TaskStatus.PROCESSING.name());
        task.setUpdatedAt(now);
        asyncTaskRepository.update(task);

        document.setStatus(DocumentStatus.INDEXING.name());
        document.setUpdatedAt(now);
        documentRepository.update(document);

        try {
            List<DocumentSegmentEntity> segments = buildSegments(document);
            documentRepository.replaceSegments(documentId, segments);
            vectorKnowledgeStore.indexSegments(document.getKnowledgeBaseId(), documentId, segments);

            document.setStatus(DocumentStatus.INDEXED.name());
            document.setErrorMessage(null);
            document.setUpdatedAt(LocalDateTime.now());
            documentRepository.update(document);

            task.setStatus(TaskStatus.SUCCESS.name());
            task.setErrorMessage(null);
            task.setUpdatedAt(LocalDateTime.now());
            asyncTaskRepository.update(task);
        } catch (Exception exception) {
            document.setStatus(DocumentStatus.FAILED.name());
            document.setErrorMessage(exception.getMessage());
            document.setUpdatedAt(LocalDateTime.now());
            documentRepository.update(document);

            task.setStatus(TaskStatus.FAILED.name());
            task.setErrorMessage(exception.getMessage());
            task.setUpdatedAt(LocalDateTime.now());
            asyncTaskRepository.update(task);
            throw exception;
        }
    }

    private List<DocumentSegmentEntity> buildSegments(DocumentRecordEntity document) {
        String content = document.getRawContent();
        if (content == null || content.isBlank()) {
            content = "Document " + document.getFileName() + " has no parsed text, using file name as placeholder.";
        }
        // 首版采用字符级切片，后续可替换为更适合中文语义的分句/分段策略。
        List<String> chunks = TextChunker.chunk(content,
                ragPipelineProperties.getChunkSize(),
                ragPipelineProperties.getChunkOverlap());

        List<DocumentSegmentEntity> segments = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            DocumentSegmentEntity entity = new DocumentSegmentEntity();
            entity.setId(IdGenerator.nextId());
            entity.setKnowledgeBaseId(document.getKnowledgeBaseId());
            entity.setDocumentId(document.getId());
            entity.setSequenceNo(i + 1);
            entity.setContent(chunks.get(i));
            entity.setCreatedAt(LocalDateTime.now());
            segments.add(entity);
        }
        return segments;
    }
}

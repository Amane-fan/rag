package com.usts.rag.rag.service;

import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;
import com.usts.rag.common.util.IdGenerator;
import com.usts.rag.domain.entity.AsyncTaskEntity;
import com.usts.rag.domain.entity.DocumentRecordEntity;
import com.usts.rag.domain.enums.DocumentStatus;
import com.usts.rag.domain.enums.TaskStatus;
import com.usts.rag.domain.enums.TaskType;
import com.usts.rag.domain.repository.AsyncTaskRepository;
import com.usts.rag.domain.repository.DocumentRepository;
import com.usts.rag.domain.repository.KnowledgeBaseRepository;
import com.usts.rag.domain.port.TaskPublisher;
import com.usts.rag.rag.model.DocumentUploadCommand;
import com.usts.rag.rag.model.DocumentUploadResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentApplicationService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final DocumentRepository documentRepository;
    private final AsyncTaskRepository asyncTaskRepository;
    private final TaskPublisher taskPublisher;

    public DocumentApplicationService(KnowledgeBaseRepository knowledgeBaseRepository,
                                      DocumentRepository documentRepository,
                                      AsyncTaskRepository asyncTaskRepository,
                                      TaskPublisher taskPublisher) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.documentRepository = documentRepository;
        this.asyncTaskRepository = asyncTaskRepository;
        this.taskPublisher = taskPublisher;
    }

    @Transactional
    public DocumentUploadResult upload(DocumentUploadCommand command) {
        knowledgeBaseRepository.findById(command.knowledgeBaseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Knowledge base not found"));

        LocalDateTime now = LocalDateTime.now();
        DocumentRecordEntity document = new DocumentRecordEntity();
        document.setId(IdGenerator.nextId());
        document.setKnowledgeBaseId(command.knowledgeBaseId());
        document.setFileName(command.fileName());
        document.setContentType(command.contentType());
        document.setRawContent(command.rawContent());
        document.setStatus(DocumentStatus.UPLOADED.name());
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        documentRepository.save(document);

        AsyncTaskEntity task = new AsyncTaskEntity();
        task.setId(IdGenerator.nextId());
        task.setBusinessId(document.getId());
        task.setTaskType(TaskType.DOCUMENT_INDEX.name());
        task.setStatus(TaskStatus.PENDING.name());
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        asyncTaskRepository.save(task);

        taskPublisher.publishDocumentIndexTask(task.getId(), document.getId());
        return new DocumentUploadResult(document.getId(), task.getId(), task.getStatus());
    }

    public List<DocumentRecordEntity> listByKnowledgeBaseId(String knowledgeBaseId) {
        return documentRepository.findByKnowledgeBaseId(knowledgeBaseId);
    }

    public DocumentRecordEntity getById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Document not found"));
    }
}

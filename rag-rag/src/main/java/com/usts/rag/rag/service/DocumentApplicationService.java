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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档应用服务。
 * <p>
 * 负责处理“上传即入库、入库即投递索引任务”的主链路，不直接关心 MQ 或向量化细节。
 */
@Service
public class DocumentApplicationService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final DocumentRepository documentRepository;
    private final AsyncTaskRepository asyncTaskRepository;
    private final TaskPublisher taskPublisher;
    private final DocumentTextExtractor documentTextExtractor;
    private final DocumentContentService documentContentService;

    public DocumentApplicationService(KnowledgeBaseRepository knowledgeBaseRepository,
                                      DocumentRepository documentRepository,
                                      AsyncTaskRepository asyncTaskRepository,
                                      TaskPublisher taskPublisher,
                                      DocumentTextExtractor documentTextExtractor,
                                      DocumentContentService documentContentService) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.documentRepository = documentRepository;
        this.asyncTaskRepository = asyncTaskRepository;
        this.taskPublisher = taskPublisher;
        this.documentTextExtractor = documentTextExtractor;
        this.documentContentService = documentContentService;
    }

    /**
     * 保存原始文档记录，并创建一条待执行的文档索引任务。
     * <p>
     * 这里使用事务保证文档记录和异步任务要么同时成功，要么同时回滚。
     */
    @Transactional
    public DocumentUploadResult upload(DocumentUploadCommand command) {
        // 判断知识库是否存在
        knowledgeBaseRepository.findById(command.knowledgeBaseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Knowledge base not found"));

        String documentId = IdGenerator.nextId();
        String parsedContent = documentTextExtractor.extract(command.fileName(), command.contentType(), command.fileBytes());
        // 将解析出的纯文本存储到阿里云OSS中，返回对应的key
        String contentStorageKey = documentContentService.storeParsedContent(
                command.knowledgeBaseId(),
                documentId,
                command.fileName(),
                command.contentType(),
                parsedContent);
        registerRollbackCleanup(contentStorageKey);

        try {
            // 保存文档 pending -> uploaded
            LocalDateTime now = LocalDateTime.now();
            DocumentRecordEntity document = new DocumentRecordEntity();
            document.setId(documentId);
            document.setKnowledgeBaseId(command.knowledgeBaseId());
            document.setFileName(command.fileName());
            document.setContentType(command.contentType());
            document.setContentStorageKey(contentStorageKey);
            document.setStatus(DocumentStatus.UPLOADED.name());
            document.setCreatedAt(now);
            document.setUpdatedAt(now);
            documentRepository.save(document);

            // 文档入库后立即创建索引任务，后续由 MQ 消费端异步执行切片和向量化。
            AsyncTaskEntity task = new AsyncTaskEntity();
            task.setId(IdGenerator.nextId());
            task.setBusinessId(document.getId());
            task.setTaskType(TaskType.DOCUMENT_INDEX.name());
            task.setStatus(TaskStatus.PENDING.name());
            task.setCreatedAt(now);
            task.setUpdatedAt(now);
            asyncTaskRepository.save(task);

            // 发布文档索引任务
            taskPublisher.publishDocumentIndexTask(task.getId(), document.getId());
            return new DocumentUploadResult(document.getId(), task.getId(), task.getStatus());
        } catch (RuntimeException exception) {
            documentContentService.delete(contentStorageKey);
            throw exception;
        }
    }

    /**
     * 按知识库查询文档列表。
     */
    public List<DocumentRecordEntity> listByKnowledgeBaseId(String knowledgeBaseId) {
        return documentRepository.findByKnowledgeBaseId(knowledgeBaseId);
    }

    /**
     * 查询单个文档详情。
     */
    public DocumentRecordEntity getById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Document not found"));
    }

    public String loadContent(DocumentRecordEntity document) {
        return documentContentService.load(document);
    }

    private void registerRollbackCleanup(String contentStorageKey) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                        documentContentService.delete(contentStorageKey);
                    }
                }
            });
        }
    }
}

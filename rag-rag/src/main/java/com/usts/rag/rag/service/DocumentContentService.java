package com.usts.rag.rag.service;

import com.usts.rag.domain.entity.DocumentRecordEntity;
import com.usts.rag.domain.port.DocumentContentStorage;
import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 文档正文访问服务。
 * <p>
 * 负责统一封装正文在对象存储中的读写，并兼容历史数据仍落在数据库中的场景。
 */
@Service
public class DocumentContentService {

    private final DocumentContentStorage documentContentStorage;

    public DocumentContentService(DocumentContentStorage documentContentStorage) {
        this.documentContentStorage = documentContentStorage;
    }

    public String storeParsedContent(String knowledgeBaseId,
                                     String documentId,
                                     String fileName,
                                     String contentType,
                                     String content) {
        return documentContentStorage.store(knowledgeBaseId, documentId, fileName, contentType, content);
    }

    public String load(DocumentRecordEntity document) {
        if (StringUtils.hasText(document.getContentStorageKey())) {
            return documentContentStorage.load(document.getContentStorageKey());
        }
        throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                "Document content storage key is missing for document: " + document.getId());
    }

    public void delete(String storageKey) {
        if (StringUtils.hasText(storageKey)) {
            documentContentStorage.delete(storageKey);
        }
    }
}

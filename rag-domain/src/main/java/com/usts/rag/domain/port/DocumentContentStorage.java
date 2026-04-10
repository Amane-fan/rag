package com.usts.rag.domain.port;

public interface DocumentContentStorage {

    String store(String knowledgeBaseId, String documentId, String fileName, String contentType, String content);

    String load(String storageKey);

    void delete(String storageKey);
}

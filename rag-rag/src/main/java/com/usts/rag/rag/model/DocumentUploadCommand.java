package com.usts.rag.rag.model;

public record DocumentUploadCommand(String knowledgeBaseId, String fileName, String contentType, byte[] fileBytes) {
}

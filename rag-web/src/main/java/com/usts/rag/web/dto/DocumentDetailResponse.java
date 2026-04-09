package com.usts.rag.web.dto;

import java.time.LocalDateTime;

public record DocumentDetailResponse(
        String id,
        String knowledgeBaseId,
        String fileName,
        String contentType,
        String status,
        String errorMessage,
        String rawContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

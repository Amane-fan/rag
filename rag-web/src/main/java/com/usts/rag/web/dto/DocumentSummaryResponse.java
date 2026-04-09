package com.usts.rag.web.dto;

import java.time.LocalDateTime;

public record DocumentSummaryResponse(
        String id,
        String knowledgeBaseId,
        String fileName,
        String contentType,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

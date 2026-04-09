package com.usts.rag.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateKnowledgeBaseRequest(
        @NotBlank String name,
        String description,
        String embeddingModel,
        @Min(1) Integer topK
) {
}

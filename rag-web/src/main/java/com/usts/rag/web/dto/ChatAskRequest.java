package com.usts.rag.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatAskRequest(
        @NotBlank String knowledgeBaseId,
        @NotBlank String question,
        Integer topK
) {
}

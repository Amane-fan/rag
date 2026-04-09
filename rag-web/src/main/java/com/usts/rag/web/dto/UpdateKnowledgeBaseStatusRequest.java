package com.usts.rag.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateKnowledgeBaseStatusRequest(@NotBlank String status) {
}

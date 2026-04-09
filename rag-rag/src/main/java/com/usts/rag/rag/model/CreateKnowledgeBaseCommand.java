package com.usts.rag.rag.model;

public record CreateKnowledgeBaseCommand(String name, String description, String embeddingModel, Integer topK) {
}

package com.usts.rag.rag.model;

public record ChatAskCommand(String knowledgeBaseId, String question, Integer topK) {
}

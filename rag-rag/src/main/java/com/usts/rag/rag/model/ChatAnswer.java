package com.usts.rag.rag.model;

import com.usts.rag.domain.model.RetrievedSegment;

import java.util.List;

public record ChatAnswer(String knowledgeBaseId, String question, String answer, List<RetrievedSegment> hits) {
}

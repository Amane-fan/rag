package com.usts.rag.domain.model;

public record RetrievedSegment(String segmentId, String documentId, String content, double score) {
}

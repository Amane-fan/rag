package com.usts.rag.domain.port;

import com.usts.rag.domain.entity.DocumentSegmentEntity;
import com.usts.rag.domain.model.RetrievedSegment;

import java.util.List;

public interface VectorKnowledgeStore {

    void indexSegments(String knowledgeBaseId, String documentId, List<DocumentSegmentEntity> segments);

    List<RetrievedSegment> search(String knowledgeBaseId, String query, int topK);
}

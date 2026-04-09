package com.usts.rag.infra.port;

import com.usts.rag.domain.entity.DocumentSegmentEntity;
import com.usts.rag.domain.model.RetrievedSegment;
import com.usts.rag.domain.port.VectorKnowledgeStore;
import com.usts.rag.domain.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量知识库适配器。
 * <p>
 * 优先使用 Spring AI VectorStore 检索；若向量库不可用，则退化为基于分片文本的简单匹配检索。
 */
@Component
public class SpringAiVectorKnowledgeStore implements VectorKnowledgeStore {

    private static final Logger log = LoggerFactory.getLogger(SpringAiVectorKnowledgeStore.class);

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;

    public SpringAiVectorKnowledgeStore(ObjectProvider<VectorStore> vectorStoreProvider,
                                        DocumentRepository documentRepository) {
        this.vectorStore = vectorStoreProvider.getIfAvailable();
        this.documentRepository = documentRepository;
    }

    @Override
    public void indexSegments(String knowledgeBaseId, String documentId, List<DocumentSegmentEntity> segments) {
        if (vectorStore == null || segments.isEmpty()) {
            return;
        }
        try {
            List<org.springframework.ai.document.Document> documents = new ArrayList<>(segments.size());
            for (DocumentSegmentEntity segment : segments) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("knowledgeBaseId", knowledgeBaseId);
                metadata.put("documentId", documentId);
                metadata.put("segmentId", segment.getId());
                documents.add(new org.springframework.ai.document.Document(segment.getContent(), metadata));
            }
            vectorStore.add(documents);
        } catch (Exception exception) {
            log.warn("Vector store indexing failed, continuing with database fallback: {}", exception.getMessage());
        }
    }

    @Override
    public List<RetrievedSegment> search(String knowledgeBaseId, String query, int topK) {
        List<RetrievedSegment> vectorResults = searchVectorStore(knowledgeBaseId, query, topK);
        if (!vectorResults.isEmpty()) {
            return vectorResults;
        }
        return searchFallback(knowledgeBaseId, query, topK);
    }

    private List<RetrievedSegment> searchVectorStore(String knowledgeBaseId, String query, int topK) {
        if (vectorStore == null) {
            return List.of();
        }
        try {
            List<org.springframework.ai.document.Document> results = vectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(topK * 3).build());
            if (results == null) {
                return List.of();
            }
            return results.stream()
                    .filter(document -> knowledgeBaseId.equals(String.valueOf(document.getMetadata().get("knowledgeBaseId"))))
                    .limit(topK)
                    .map(document -> new RetrievedSegment(
                            String.valueOf(document.getMetadata().getOrDefault("segmentId", "")),
                            String.valueOf(document.getMetadata().getOrDefault("documentId", "")),
                            document.getText(),
                            1.0d))
                    .toList();
        } catch (Exception exception) {
            log.warn("Vector search failed, using fallback search: {}", exception.getMessage());
            return List.of();
        }
    }

    private List<RetrievedSegment> searchFallback(String knowledgeBaseId, String query, int topK) {
        // 这里的降级策略只用于本地联调，不适合作为生产检索方案。
        return documentRepository.findSegmentsByKnowledgeBaseId(knowledgeBaseId, 50).stream()
                .sorted(Comparator.comparingInt(segment -> -score(segment.getContent(), query)))
                .limit(topK)
                .map(segment -> new RetrievedSegment(segment.getId(), segment.getDocumentId(), segment.getContent(),
                        score(segment.getContent(), query)))
                .toList();
    }

    private int score(String content, String query) {
        String normalizedContent = content == null ? "" : content.toLowerCase();
        String normalizedQuery = query == null ? "" : query.toLowerCase();
        int score = 0;
        for (String token : normalizedQuery.split("\\s+")) {
            if (!token.isBlank() && normalizedContent.contains(token)) {
                score += 10;
            }
        }
        score += Math.min(normalizedContent.length(), 50);
        return score;
    }
}

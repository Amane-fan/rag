package com.usts.rag.rag.service;

import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;
import com.usts.rag.domain.entity.KnowledgeBaseEntity;
import com.usts.rag.domain.enums.KnowledgeBaseStatus;
import com.usts.rag.domain.model.RetrievedSegment;
import com.usts.rag.domain.port.ChatCompletionPort;
import com.usts.rag.domain.port.VectorKnowledgeStore;
import com.usts.rag.domain.repository.KnowledgeBaseRepository;
import com.usts.rag.rag.config.RagPipelineProperties;
import com.usts.rag.rag.model.ChatAnswer;
import com.usts.rag.rag.model.ChatAskCommand;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 问答应用服务。
 * <p>
 * 负责串联知识库校验、向量检索、上下文组装和大模型回答生成。
 */
@Service
public class ChatApplicationService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final VectorKnowledgeStore vectorKnowledgeStore;
    private final ChatCompletionPort chatCompletionPort;
    private final RagPipelineProperties ragPipelineProperties;

    public ChatApplicationService(KnowledgeBaseRepository knowledgeBaseRepository,
                                  VectorKnowledgeStore vectorKnowledgeStore,
                                  ChatCompletionPort chatCompletionPort,
                                  RagPipelineProperties ragPipelineProperties) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.vectorKnowledgeStore = vectorKnowledgeStore;
        this.chatCompletionPort = chatCompletionPort;
        this.ragPipelineProperties = ragPipelineProperties;
    }

    /**
     * 执行一次基于知识库的问答请求。
     */
    public ChatAnswer ask(ChatAskCommand command) {
        KnowledgeBaseEntity knowledgeBase = knowledgeBaseRepository.findById(command.knowledgeBaseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Knowledge base not found"));
        if (!KnowledgeBaseStatus.ACTIVE.name().equals(knowledgeBase.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Knowledge base is not active");
        }

        // 调用方未显式指定 topK 时，优先使用知识库配置，再退化到系统默认值。
        int topK = command.topK() != null ? command.topK() : defaultTopK(knowledgeBase);
        List<RetrievedSegment> hits = vectorKnowledgeStore.search(command.knowledgeBaseId(), command.question(), topK);
        // 大模型接口只需要片段正文，因此这里先把检索命中的实体映射成上下文文本集合。
        List<String> contexts = hits.stream().map(RetrievedSegment::content).toList();
        String answer = chatCompletionPort.complete(command.question(), contexts);

        return new ChatAnswer(command.knowledgeBaseId(), command.question(), answer, hits);
    }

    /**
     * 获取一次问答的默认召回数量。
     */
    private int defaultTopK(KnowledgeBaseEntity knowledgeBase) {
        return knowledgeBase.getTopK() != null ? knowledgeBase.getTopK() : ragPipelineProperties.getDefaultTopK();
    }
}

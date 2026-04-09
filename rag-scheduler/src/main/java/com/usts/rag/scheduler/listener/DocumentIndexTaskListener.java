package com.usts.rag.scheduler.listener;

import com.usts.rag.domain.event.DocumentIndexEvent;
import com.usts.rag.rag.service.DocumentIndexingService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "${rag.mq.topics.document-index:rag-document-index}",
        consumerGroup = "${rag.mq.consumer-groups.document-index:rag-document-index-group}"
)
public class DocumentIndexTaskListener implements RocketMQListener<DocumentIndexEvent> {

    private final DocumentIndexingService documentIndexingService;

    public DocumentIndexTaskListener(DocumentIndexingService documentIndexingService) {
        this.documentIndexingService = documentIndexingService;
    }

    @Override
    public void onMessage(DocumentIndexEvent message) {
        documentIndexingService.process(message.taskId(), message.documentId());
    }
}

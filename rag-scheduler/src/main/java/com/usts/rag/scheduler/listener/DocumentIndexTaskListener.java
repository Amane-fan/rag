package com.usts.rag.scheduler.listener;

import com.usts.rag.domain.event.DocumentIndexEvent;
import com.usts.rag.rag.service.DocumentIndexingService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 文档索引任务消费者。
 * <p>
 * 监听 RocketMQ 中的索引消息，并把真正的业务处理委托给应用服务。
 */
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
        // MQ 消息只承载任务标识和文档标识，具体状态流转在 DocumentIndexingService 中完成。
        documentIndexingService.process(message.taskId(), message.documentId());
    }
}

package com.usts.rag.infra.port;

import com.usts.rag.domain.event.DocumentIndexEvent;
import com.usts.rag.domain.port.TaskPublisher;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 任务发布适配器。
 */
@Component
public class RocketMqTaskPublisher implements TaskPublisher {

    private static final Logger log = LoggerFactory.getLogger(RocketMqTaskPublisher.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final String topic;

    public RocketMqTaskPublisher(ObjectProvider<RocketMQTemplate> rocketMQTemplateProvider,
                                 @Value("${rag.mq.topics.document-index:rag-document-index}") String topic) {
        this.rocketMQTemplate = rocketMQTemplateProvider.getIfAvailable();
        this.topic = topic;
    }

    @Override
    public void publishDocumentIndexTask(String taskId, String documentId) {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate unavailable, document index task {} will not be published", taskId);
            return;
        }
        rocketMQTemplate.convertAndSend(topic, new DocumentIndexEvent(taskId, documentId));
    }
}

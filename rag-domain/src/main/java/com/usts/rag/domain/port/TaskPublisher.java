package com.usts.rag.domain.port;

public interface TaskPublisher {

    void publishDocumentIndexTask(String taskId, String documentId);
}

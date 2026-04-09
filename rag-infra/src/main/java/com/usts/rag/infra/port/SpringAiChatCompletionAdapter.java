package com.usts.rag.infra.port;

import com.usts.rag.domain.port.ChatCompletionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 大模型对话适配器。
 * <p>
 * 优先走 Spring AI ChatClient；在未配置模型或调用失败时降级返回 mock 结果，
 * 以保证本地骨架在基础设施未完全就绪时仍能联调。
 */
@Component
public class SpringAiChatCompletionAdapter implements ChatCompletionPort {

    private static final Logger log = LoggerFactory.getLogger(SpringAiChatCompletionAdapter.class);

    private final ChatClient chatClient;
    private final String fallbackPrefix;

    public SpringAiChatCompletionAdapter(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                                         @Value("${rag.rag.fallback-prefix:[mock-answer]}") String fallbackPrefix) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        this.chatClient = builder != null ? builder.build() : null;
        this.fallbackPrefix = fallbackPrefix;
    }

    @Override
    public String complete(String question, List<String> contexts) {
        if (chatClient == null) {
            return fallbackAnswer(question, contexts);
        }
        try {
            return chatClient.prompt()
                    .system("You answer questions based on the provided enterprise knowledge snippets.")
                    .user(buildUserPrompt(question, contexts))
                    .call()
                    .content();
        } catch (Exception exception) {
            log.warn("Spring AI chat call failed, falling back to mock answer: {}", exception.getMessage());
            return fallbackAnswer(question, contexts);
        }
    }

    private String buildUserPrompt(String question, List<String> contexts) {
        if (contexts.isEmpty()) {
            return "Question:\n" + question + "\n\nNo knowledge snippets were retrieved.";
        }
        // 将检索出的片段直接拼入提示词，便于快速搭建最小可运行 RAG 流程。
        return "Question:\n" + question + "\n\nKnowledge snippets:\n- " + String.join("\n- ", contexts);
    }

    private String fallbackAnswer(String question, List<String> contexts) {
        String context = contexts.isEmpty() ? "No retrieved context." : contexts.get(0);
        return fallbackPrefix + " " + question + " | context: " + context;
    }
}

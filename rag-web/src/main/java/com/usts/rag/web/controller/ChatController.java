package com.usts.rag.web.controller;

import com.usts.rag.common.api.ApiResponse;
import com.usts.rag.rag.model.ChatAnswer;
import com.usts.rag.rag.model.ChatAskCommand;
import com.usts.rag.rag.service.ChatApplicationService;
import com.usts.rag.web.dto.ChatAskRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatApplicationService chatApplicationService;

    public ChatController(ChatApplicationService chatApplicationService) {
        this.chatApplicationService = chatApplicationService;
    }

    @PostMapping("/ask")
    public ApiResponse<ChatAnswer> ask(@Valid @RequestBody ChatAskRequest request) {
        ChatAnswer answer = chatApplicationService.ask(new ChatAskCommand(
                request.knowledgeBaseId(),
                request.question(),
                request.topK()));
        return ApiResponse.success(answer);
    }
}

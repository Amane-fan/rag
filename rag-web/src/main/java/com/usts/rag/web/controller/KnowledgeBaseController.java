package com.usts.rag.web.controller;

import com.usts.rag.common.api.ApiResponse;
import com.usts.rag.domain.entity.KnowledgeBaseEntity;
import com.usts.rag.domain.enums.KnowledgeBaseStatus;
import com.usts.rag.rag.model.CreateKnowledgeBaseCommand;
import com.usts.rag.rag.service.KnowledgeBaseApplicationService;
import com.usts.rag.web.dto.CreateKnowledgeBaseRequest;
import com.usts.rag.web.dto.UpdateKnowledgeBaseStatusRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识库管理接口。
 */
@RestController
@RequestMapping("/api/v1/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseApplicationService knowledgeBaseApplicationService;

    public KnowledgeBaseController(KnowledgeBaseApplicationService knowledgeBaseApplicationService) {
        this.knowledgeBaseApplicationService = knowledgeBaseApplicationService;
    }

    @PostMapping
    public ApiResponse<KnowledgeBaseEntity> create(@Valid @RequestBody CreateKnowledgeBaseRequest request) {
        KnowledgeBaseEntity entity = knowledgeBaseApplicationService.create(new CreateKnowledgeBaseCommand(
                request.name(),
                request.description(),
                request.embeddingModel(),
                request.topK()));
        return ApiResponse.success(entity);
    }

    @GetMapping
    public ApiResponse<List<KnowledgeBaseEntity>> list() {
        return ApiResponse.success(knowledgeBaseApplicationService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBaseEntity> get(@PathVariable String id) {
        return ApiResponse.success(knowledgeBaseApplicationService.get(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<KnowledgeBaseEntity> updateStatus(@PathVariable String id,
                                                         @Valid @RequestBody UpdateKnowledgeBaseStatusRequest request) {
        return ApiResponse.success(knowledgeBaseApplicationService.changeStatus(
                id,
                KnowledgeBaseStatus.valueOf(request.status())));
    }
}

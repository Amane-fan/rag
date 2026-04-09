package com.usts.rag.web.controller;

import com.usts.rag.common.api.ApiResponse;
import com.usts.rag.domain.entity.DocumentRecordEntity;
import com.usts.rag.rag.model.DocumentUploadCommand;
import com.usts.rag.rag.model.DocumentUploadResult;
import com.usts.rag.rag.service.DocumentApplicationService;
import com.usts.rag.web.dto.DocumentDetailResponse;
import com.usts.rag.web.dto.DocumentSummaryResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文档管理接口。
 * <p>
 * 负责接收上传请求，并把结果转换成前端更容易消费的响应结构。
 */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentApplicationService documentApplicationService;

    public DocumentController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    /**
     * 上传文档并创建异步索引任务。
     * <p>
     * 文件解析由应用层统一处理，支持通过 Apache Tika 提取常见文档格式中的文本。
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DocumentUploadResult> upload(@RequestParam("knowledgeBaseId") String knowledgeBaseId,
                                                    @RequestPart("file") MultipartFile file) throws IOException {
        String fileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "unnamed.txt";
        DocumentUploadResult result = documentApplicationService.upload(new DocumentUploadCommand(
                knowledgeBaseId,
                fileName,
                file.getContentType(),
                file.getBytes()));
        return ApiResponse.success(result);
    }

    /**
     * 查询指定知识库下的文档列表。
     */
    @GetMapping
    public ApiResponse<List<DocumentSummaryResponse>> list(@RequestParam("knowledgeBaseId") String knowledgeBaseId) {
        List<DocumentSummaryResponse> items = documentApplicationService.listByKnowledgeBaseId(knowledgeBaseId).stream()
                .map(document -> new DocumentSummaryResponse(document.getId(), document.getKnowledgeBaseId(),
                        document.getFileName(), document.getContentType(), document.getStatus(),
                        document.getCreatedAt(), document.getUpdatedAt()))
                .toList();
        return ApiResponse.success(items);
    }

    /**
     * 查询单个文档详情，便于排查原文、索引状态和错误信息。
     */
    @GetMapping("/detail")
    public ApiResponse<DocumentDetailResponse> detail(@RequestParam("id") String id) {
        DocumentRecordEntity document = documentApplicationService.getById(id);
        return ApiResponse.success(new DocumentDetailResponse(document.getId(), document.getKnowledgeBaseId(),
                document.getFileName(), document.getContentType(), document.getStatus(),
                document.getErrorMessage(), document.getRawContent(), document.getCreatedAt(), document.getUpdatedAt()));
    }
}

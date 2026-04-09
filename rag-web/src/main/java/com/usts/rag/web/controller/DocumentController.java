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
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentApplicationService documentApplicationService;

    public DocumentController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DocumentUploadResult> upload(@RequestParam("knowledgeBaseId") String knowledgeBaseId,
                                                    @RequestPart("file") MultipartFile file) throws IOException {
        String fileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "unnamed.txt";
        String rawContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        DocumentUploadResult result = documentApplicationService.upload(new DocumentUploadCommand(
                knowledgeBaseId,
                fileName,
                file.getContentType(),
                rawContent));
        return ApiResponse.success(result);
    }

    @GetMapping
    public ApiResponse<List<DocumentSummaryResponse>> list(@RequestParam("knowledgeBaseId") String knowledgeBaseId) {
        List<DocumentSummaryResponse> items = documentApplicationService.listByKnowledgeBaseId(knowledgeBaseId).stream()
                .map(document -> new DocumentSummaryResponse(document.getId(), document.getKnowledgeBaseId(),
                        document.getFileName(), document.getContentType(), document.getStatus(),
                        document.getCreatedAt(), document.getUpdatedAt()))
                .toList();
        return ApiResponse.success(items);
    }

    @GetMapping("/detail")
    public ApiResponse<DocumentDetailResponse> detail(@RequestParam("id") String id) {
        DocumentRecordEntity document = documentApplicationService.getById(id);
        return ApiResponse.success(new DocumentDetailResponse(document.getId(), document.getKnowledgeBaseId(),
                document.getFileName(), document.getContentType(), document.getStatus(),
                document.getErrorMessage(), document.getRawContent(), document.getCreatedAt(), document.getUpdatedAt()));
    }
}

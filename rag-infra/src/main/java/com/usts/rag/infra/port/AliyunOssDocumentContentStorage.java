package com.usts.rag.infra.port;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;
import com.usts.rag.domain.port.DocumentContentStorage;
import com.usts.rag.infra.config.OssStorageProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 基于阿里云 OSS 的文档正文存储实现。
 */
@Component
public class AliyunOssDocumentContentStorage implements DocumentContentStorage {

    private static final String STORED_CONTENT_TYPE = "text/plain; charset=UTF-8";

    private final OssStorageProperties properties;
    private volatile OSS ossClient;

    public AliyunOssDocumentContentStorage(OssStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String store(String knowledgeBaseId, String documentId, String fileName, String contentType, String content) {
        String storageKey = buildStorageKey(knowledgeBaseId, documentId);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(STORED_CONTENT_TYPE);
        metadata.setContentLength(bytes.length);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            getClient().putObject(new PutObjectRequest(properties.getBucket(), storageKey, inputStream, metadata));
            return storageKey;
        } catch (IOException | OSSException | ClientException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "Failed to store document content to OSS: " + safeFileName(fileName));
        }
    }

    @Override
    public String load(String storageKey) {
        OSSObject object = getClient().getObject(properties.getBucket(), storageKey);
        try (InputStream inputStream = object.getObjectContent()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | OSSException | ClientException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "Failed to load document content from OSS: " + storageKey);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            getClient().deleteObject(properties.getBucket(), storageKey);
        } catch (OSSException | ClientException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "Failed to delete document content from OSS: " + storageKey);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    private OSS getClient() {
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    validateProperties();
                    ossClient = new OSSClientBuilder().build(
                            properties.getEndpoint(),
                            properties.getAccessKeyId(),
                            properties.getAccessKeySecret());
                }
            }
        }
        return ossClient;
    }

    private void validateProperties() {
        if (!StringUtils.hasText(properties.getEndpoint())
                || !StringUtils.hasText(properties.getBucket())
                || !StringUtils.hasText(properties.getAccessKeyId())
                || !StringUtils.hasText(properties.getAccessKeySecret())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "OSS storage is not configured. Please set rag.storage.oss.endpoint, bucket, access-key-id and access-key-secret");
        }
    }

    private String buildStorageKey(String knowledgeBaseId, String documentId) {
        String prefix = StringUtils.hasText(properties.getKeyPrefix()) ? properties.getKeyPrefix().trim() : "document-content";
        return prefix + "/" + knowledgeBaseId + "/" + documentId + "/content.txt";
    }

    private String safeFileName(String fileName) {
        return StringUtils.hasText(fileName) ? fileName : "unnamed-file";
    }
}

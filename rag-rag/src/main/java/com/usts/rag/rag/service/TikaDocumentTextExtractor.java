package com.usts.rag.rag.service;

import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 基于 Apache Tika 的文档文本提取器，统一处理 txt、pdf、docx 等常见格式。
 */
@Component
public class TikaDocumentTextExtractor implements DocumentTextExtractor {

    private static final int UNLIMITED_WRITE_LIMIT = -1;

    private final AutoDetectParser parser = new AutoDetectParser();

    @Override
    public String extract(String fileName, String contentType, byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Uploaded file is empty");
        }

        Metadata metadata = new Metadata();
        if (StringUtils.hasText(fileName)) {
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
        }
        if (StringUtils.hasText(contentType)) {
            metadata.set(Metadata.CONTENT_TYPE, contentType);
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            BodyContentHandler handler = new BodyContentHandler(UNLIMITED_WRITE_LIMIT);
            parser.parse(inputStream, handler, metadata, new ParseContext());
            String normalized = normalize(handler.toString());
            if (normalized.isBlank()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        "No readable text could be extracted from file: " + safeFileName(fileName));
            }
            return normalized;
        } catch (IOException | SAXException | TikaException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Failed to extract text from file: " + safeFileName(fileName));
        }
    }

    private String normalize(String text) {
        return text == null ? "" : text.replace("\u0000", "")
                .replaceAll("\\R{3,}", "\n\n")
                .trim();
    }

    private String safeFileName(String fileName) {
        return StringUtils.hasText(fileName) ? fileName : "unnamed-file";
    }
}

package com.usts.rag.rag.service;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单文本切片工具。
 * <p>
 * 目前采用固定窗口 + 重叠字符的方式，便于快速搭建可运行骨架。
 */
public final class TextChunker {

    private TextChunker() {
    }

    public static List<String> chunk(String content, int chunkSize, int chunkOverlap) {
        String normalized = content.strip();
        if (normalized.isEmpty()) {
            return List.of("Empty document");
        }
        if (normalized.length() <= chunkSize) {
            return List.of(normalized);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        int step = Math.max(1, chunkSize - chunkOverlap);
        while (start < normalized.length()) {
            int end = Math.min(normalized.length(), start + chunkSize);
            chunks.add(normalized.substring(start, end));
            if (end == normalized.length()) {
                break;
            }
            start += step;
        }
        return chunks;
    }
}

package com.usts.rag.rag.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rag.rag")
public class RagPipelineProperties {

    @Min(1)
    private int defaultTopK = 4;
    @Min(100)
    private int chunkSize = 500;
    @Min(0)
    private int chunkOverlap = 100;
    @NotBlank
    private String fallbackPrefix = "[mock-answer]";

    public int getDefaultTopK() {
        return defaultTopK;
    }

    public void setDefaultTopK(int defaultTopK) {
        this.defaultTopK = defaultTopK;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }

    public String getFallbackPrefix() {
        return fallbackPrefix;
    }

    public void setFallbackPrefix(String fallbackPrefix) {
        this.fallbackPrefix = fallbackPrefix;
    }
}

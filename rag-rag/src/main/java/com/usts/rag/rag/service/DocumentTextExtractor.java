package com.usts.rag.rag.service;

public interface DocumentTextExtractor {

    String extract(String fileName, String contentType, byte[] fileBytes);
}

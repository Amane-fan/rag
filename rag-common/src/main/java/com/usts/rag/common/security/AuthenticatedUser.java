package com.usts.rag.common.security;

public record AuthenticatedUser(String userId, String username, String displayName) {
}

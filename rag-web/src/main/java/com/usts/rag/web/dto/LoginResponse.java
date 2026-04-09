package com.usts.rag.web.dto;

import com.usts.rag.common.security.AuthenticatedUser;

import java.time.Instant;

public record LoginResponse(String accessToken, Instant expiresAt, AuthenticatedUser user) {
}

package com.usts.rag.web.controller;

import com.usts.rag.common.api.ApiResponse;
import com.usts.rag.common.security.AuthenticatedUser;
import com.usts.rag.rag.model.LoginCommand;
import com.usts.rag.rag.service.AuthApplicationService;
import com.usts.rag.web.config.JwtProperties;
import com.usts.rag.web.dto.LoginRequest;
import com.usts.rag.web.dto.LoginResponse;
import com.usts.rag.web.security.JwtTokenProvider;
import com.usts.rag.web.security.RedisSessionStore;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisSessionStore redisSessionStore;
    private final JwtProperties jwtProperties;

    public AuthController(AuthApplicationService authApplicationService,
                          JwtTokenProvider jwtTokenProvider,
                          RedisSessionStore redisSessionStore,
                          JwtProperties jwtProperties) {
        this.authApplicationService = authApplicationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisSessionStore = redisSessionStore;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticatedUser user = authApplicationService.authenticate(new LoginCommand(request.username(), request.password()));
        JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.createToken(user);
        redisSessionStore.save(tokenPair.sessionId(), user, jwtProperties.getTtl());
        return ApiResponse.success(new LoginResponse(tokenPair.token(), tokenPair.expiresAt(), user));
    }

    @GetMapping("/me")
    public ApiResponse<AuthenticatedUser> me(Authentication authentication) {
        return ApiResponse.success((AuthenticatedUser) authentication.getPrincipal());
    }
}

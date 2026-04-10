package com.usts.rag.web.security;

import com.usts.rag.common.security.UserContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usts.rag.common.api.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * JWT 认证过滤器。
 * <p>
 * 从请求头提取 token，解析会话信息，并把当前用户写入 ThreadLocal 上下文。
 * 对需要登录的接口，若没有拿到合法会话则直接返回 401。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/login",
            "/actuator/health",
            "/actuator/info",
            "/error");

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisSessionStore redisSessionStore;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   RedisSessionStore redisSessionStore,
                                   ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisSessionStore = redisSessionStore;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取token以及用户信息
        try {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7).trim();
                Claims claims = jwtTokenProvider.parseClaims(token);
                String sessionId = claims.get("sessionId", String.class);
                // 如果redis中存在用户信息，将其写入threadLocal
                redisSessionStore.get(sessionId).ifPresent(UserContextHolder::set);
            }
        } catch (Exception ignored) {
            UserContextHolder.clear();
        }

        try {
            // 如果属于需要登录的接口，但是没有用户上下文，直接返回401
            if (requiresAuthentication(request) && UserContextHolder.get().isEmpty()) {
                writeUnauthorized(response);
                return;
            }
            filterChain.doFilter(request, response);
        } finally {
            // 由于threadLocal绑定的是容器线程，web容器线程可能有复用，避免其他线程拿到了该用户信息
            UserContextHolder.clear();
        }
    }

    // 判断是否是白名单
    private boolean requiresAuthentication(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return !PUBLIC_PATHS.contains(request.getServletPath());
    }

    // 返回错误信息（不在白名单但是访问了需要登录的接口）
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(),
                ApiResponse.failure("UNAUTHORIZED", "Authentication required"));
    }
}

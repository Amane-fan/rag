package com.usts.rag.common.security;

import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;

import java.util.Optional;

/**
 * 基于 ThreadLocal 的用户上下文容器。
 * <p>
 * 过滤器在请求进入时写入当前登录用户，请求结束后必须主动清理，
 * 避免容器线程复用时把上一次请求的用户信息泄露到下一次请求。
 */
public final class UserContextHolder {

    private static final ThreadLocal<AuthenticatedUser> USER_CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(AuthenticatedUser user) {
        USER_CONTEXT.set(user);
    }

    public static Optional<AuthenticatedUser> get() {
        return Optional.ofNullable(USER_CONTEXT.get());
    }

    public static AuthenticatedUser requireUser() {
        return get().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication required"));
    }

    public static void clear() {
        USER_CONTEXT.remove();
    }
}

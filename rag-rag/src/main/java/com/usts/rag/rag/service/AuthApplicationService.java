package com.usts.rag.rag.service;

import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;
import com.usts.rag.common.security.AuthenticatedUser;
import com.usts.rag.rag.config.AdminAuthProperties;
import com.usts.rag.rag.model.LoginCommand;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private final AdminAuthProperties adminAuthProperties;

    public AuthApplicationService(AdminAuthProperties adminAuthProperties) {
        this.adminAuthProperties = adminAuthProperties;
    }

    /**
     * 首版仅提供平台管理员登录，后续可平滑扩展为数据库用户体系或 SSO。
     */
    public AuthenticatedUser authenticate(LoginCommand command) {
        boolean matched = adminAuthProperties.getUsername().equals(command.username())
                && adminAuthProperties.getPassword().equals(command.password());
        if (!matched) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid username or password");
        }
        return new AuthenticatedUser("admin", adminAuthProperties.getUsername(), adminAuthProperties.getDisplayName());
    }
}

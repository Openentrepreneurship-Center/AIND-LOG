package com.backend.global.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.backend.domain.admin.entity.AuditLog;
import com.backend.domain.admin.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(adminAudit)")
    public Object audit(ProceedingJoinPoint joinPoint, AdminAudit adminAudit) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            String adminId = extractAdminId();
            String targetId = extractTargetId(joinPoint);
            String ipAddress = extractIpAddress();

            AuditLog auditLog = AuditLog.builder()
                    .adminId(adminId != null ? adminId : "UNKNOWN")
                    .action(adminAudit.action())
                    .targetType(adminAudit.targetType().isEmpty() ? null : adminAudit.targetType())
                    .targetId(targetId)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("감사 로그 기록 실패: action={}", adminAudit.action(), e);
        }

        return result;
    }

    private String extractAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String principal
                && !"anonymousUser".equals(principal)) {
            return principal;
        }
        return null;
    }

    private String extractTargetId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof String firstArg) {
            return firstArg;
        }
        return null;
    }

    private String extractIpAddress() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return null;
    }
}

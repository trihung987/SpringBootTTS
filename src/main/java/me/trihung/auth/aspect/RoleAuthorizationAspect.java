package me.trihung.auth.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import me.trihung.auth.annotation.HasAnyRole;
import me.trihung.exception.UnauthorizedException;

import java.util.Arrays;
import java.util.Collection;

@Aspect
@Component
public class RoleAuthorizationAspect {
	
	//AOP chạy checkRole khi các method có annotation hasAnyRole để check trước khi chạy
    @Around("@annotation(me.trihung.auth.annotation.HasAnyRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        HasAnyRole hasAnyRole = signature.getMethod().getAnnotation(HasAnyRole.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw UnauthorizedException.message("User is not authenticated");
        }

        Collection<String> authorities = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("SCOPE_", ""))
                .toList();

        boolean hasRequiredRole = authorities.stream()
                .anyMatch(authority -> Arrays.stream(hasAnyRole.value())
                        .anyMatch(authority::contains));

        if (!hasRequiredRole) {
            throw UnauthorizedException.message("User does not have required role");
        }

        return joinPoint.proceed();
    }
}

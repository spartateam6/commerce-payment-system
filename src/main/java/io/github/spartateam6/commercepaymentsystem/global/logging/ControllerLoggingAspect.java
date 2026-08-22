package io.github.spartateam6.commercepaymentsystem.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {
    // Controller 공개 메서드만 공통 로그 대상
    @Around("execution(public * io.github.spartateam6.commercepaymentsystem.domain..controller..*.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        HttpServletRequest httpServletRequest = getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String httpMethod = httpServletRequest != null ? httpServletRequest.getMethod() : "UNKNOWN";
        String requestUri = httpServletRequest != null ? httpServletRequest.getRequestURI() : "UNKNOWN";
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        //Controller 요청 처리가 된 시점
        log.info("[요청 시작] httpMethod={} uri={} controller={}.{}()",
                httpMethod, requestUri, className, methodName);

        try {
            Object result = joinPoint.proceed();

            //정상 응답까지 걸린 시간
            log.info("[요청 완료] httpMethod={} uri={} controller={}.{}() durationMs={}",
                    httpMethod, requestUri, className, methodName, elapsedMillis(startNanos));
            return result;
        } catch (Throwable throwable) {
            // 실패한 요청의 예외 유형과 처리시간을 남긴다.
            log.warn("[요청 실패] httpMethod={} uri={} controller={}.{}() durationMs={} exceptionType={}",
                    httpMethod, requestUri, className, methodName, elapsedMillis(startNanos), throwable.getClass().getSimpleName());
            //응답 생성은 글로발익쌕쎤핸들러에게,,
            throw throwable;
        }

    }

    private HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (attributes instanceof ServletRequestAttributes requestAttributes) {
            return requestAttributes.getRequest();
        }
        return null;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}

package com.example.demoapp.infrastructure.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * AOP 切面類，用於記錄所有 API 請求和響應的日誌
 * 提供詳細的請求追蹤和性能監控
 */
@Aspect
@Component
public class ApiLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 定義切點：匹配所有控制器中的公共方法
     */
    @Pointcut("execution(public * com.example.demoapp.adapter.web.controller..*(..))")
    public void controllerMethods() {}
    
    /**
     * 定義切點：匹配帶有 @ApiLog 註解的方法
     */
    @Pointcut("@annotation(com.example.demoapp.infrastructure.logging.ApiLog)")
    public void apiLogAnnotatedMethods() {}

    /**
     * 環繞通知：記錄請求開始、結束和執行時間
     */
    @Around("controllerMethods() || apiLogAnnotatedMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        // 設置 MDC 用於追蹤
        MDC.put("requestId", requestId);
        MDC.put("timestamp", LocalDateTime.now().format(formatter));
        
        try {
            // 記錄請求開始
            logRequestStart(joinPoint, requestId);
            
            // 執行實際方法
            Object result = joinPoint.proceed();
            
            // 記錄請求成功完成
            logRequestSuccess(joinPoint, result, requestId, startTime);
            
            // 檢查是否為慢查詢
            checkSlowQuery(joinPoint, requestId, startTime);
            
            return result;
            
        } catch (Exception e) {
            // 記錄請求異常
            logRequestError(joinPoint, e, requestId, startTime);
            throw e;
        } finally {
            // 清理 MDC
            MDC.clear();
        }
    }

    /**
     * 記錄請求開始
     */
    private void logRequestStart(JoinPoint joinPoint, String requestId) {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String queryString = request.getQueryString();
                String clientIp = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");
                
                logger.info("API_REQUEST_START - RequestId: {}, Method: {}, URI: {}, QueryString: {}, ClientIP: {}, UserAgent: {}, Controller: {}, Method: {}, Args: {}", 
                    requestId, 
                    method, 
                    uri, 
                    queryString != null ? queryString : "N/A",
                    clientIp,
                    userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 100)) : "N/A",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    sanitizeArgs(joinPoint.getArgs())
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to log request start: {}", e.getMessage());
        }
    }

    /**
     * 記錄請求成功完成
     */
    private void logRequestSuccess(JoinPoint joinPoint, Object result, String requestId, long startTime) {
        try {
            long duration = System.currentTimeMillis() - startTime;
            HttpServletRequest request = getCurrentRequest();
            
            if (request != null) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                
                // 獲取響應狀態碼
                int statusCode = 200; // 默認值
                if (result instanceof ResponseEntity) {
                    statusCode = ((ResponseEntity<?>) result).getStatusCode().value();
                }
                
                logger.info("API_REQUEST_SUCCESS - RequestId: {}, Method: {}, URI: {}, StatusCode: {}, Duration: {}ms, Controller: {}, Method: {}, ResponseType: {}", 
                    requestId,
                    method,
                    uri,
                    statusCode,
                    duration,
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    result != null ? result.getClass().getSimpleName() : "null"
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to log request success: {}", e.getMessage());
        }
    }

    /**
     * 記錄請求異常
     */
    private void logRequestError(JoinPoint joinPoint, Exception exception, String requestId, long startTime) {
        try {
            long duration = System.currentTimeMillis() - startTime;
            HttpServletRequest request = getCurrentRequest();
            
            if (request != null) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                
                logger.error("API_REQUEST_ERROR - RequestId: {}, Method: {}, URI: {}, Duration: {}ms, Controller: {}, Method: {}, Error: {}, Message: {}", 
                    requestId,
                    method,
                    uri,
                    duration,
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    exception.getClass().getSimpleName(),
                    exception.getMessage()
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to log request error: {}", e.getMessage());
        }
    }

    /**
     * 獲取當前 HTTP 請求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 獲取客戶端真實 IP 地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 取第一個 IP（如果有多個的話）
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 清理和格式化方法參數，避免敏感信息洩露
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        try {
            Object[] sanitizedArgs = Arrays.stream(args)
                .map(this::sanitizeArg)
                .toArray();
            return Arrays.toString(sanitizedArgs);
        } catch (Exception e) {
            return "[Error serializing args: " + e.getMessage() + "]";
        }
    }

    /**
     * 清理單個參數
     */
    private Object sanitizeArg(Object arg) {
        if (arg == null) {
            return null;
        }
        
        String className = arg.getClass().getSimpleName();
        
        // 對於文件上傳，只記錄基本信息
        if (className.contains("MultipartFile")) {
            return "[MultipartFile]";
        }
        
        // 對於大型對象，只記錄類型
        if (className.contains("InputStream") || className.contains("OutputStream")) {
            return "[" + className + "]";
        }
        
        // 對於字符串，限制長度
        if (arg instanceof String) {
            String str = (String) arg;
            return str.length() > 100 ? str.substring(0, 100) + "..." : str;
        }
        
        // 對於基本類型，直接返回
        if (arg instanceof Number || arg instanceof Boolean) {
            return arg;
        }
        
        // 對於其他對象，返回類型名稱
        return "[" + className + "]";
    }

    /**
     * 檢查慢查詢並記錄警告
     */
    private void checkSlowQuery(JoinPoint joinPoint, String requestId, long startTime) {
        try {
            long duration = System.currentTimeMillis() - startTime;
            
            // 獲取 @ApiLog 註解配置
            ApiLog apiLog = joinPoint.getTarget().getClass()
                .getMethod(joinPoint.getSignature().getName(), 
                    Arrays.stream(joinPoint.getArgs())
                        .map(Object::getClass)
                        .toArray(Class[]::new))
                .getAnnotation(ApiLog.class);
            
            long threshold = apiLog != null ? apiLog.slowQueryThreshold() : 1000;
            
            if (duration > threshold) {
                HttpServletRequest request = getCurrentRequest();
                if (request != null) {
                    logger.warn("SLOW_API_DETECTED - RequestId: {}, Method: {}, URI: {}, Duration: {}ms, Threshold: {}ms, Controller: {}, Method: {}", 
                        requestId,
                        request.getMethod(),
                        request.getRequestURI(),
                        duration,
                        threshold,
                        joinPoint.getTarget().getClass().getSimpleName(),
                        joinPoint.getSignature().getName()
                    );
                }
            }
        } catch (Exception e) {
            // 忽略錯誤，避免影響正常業務流程
            logger.debug("Failed to check slow query: {}", e.getMessage());
        }
    }
}
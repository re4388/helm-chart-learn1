package com.example.demoapp.infrastructure.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for manual tracing operations
 * 手動追蹤操作的服務
 */
@Service
public class TracingService {
    
    private static final Logger LOG = LoggerFactory.getLogger(TracingService.class);
    
    private final Tracer tracer;
    
    public TracingService(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Create a custom span for business operations
     * 為業務操作創建自定義 span
     */
    public <T> T traceBusinessOperation(String operationName, String entityType, String entityId, 
                                       java.util.function.Supplier<T> operation) {
        Span span = tracer.nextSpan()
                .name(operationName)
                .tag("entity.type", entityType)
                .tag("entity.id", entityId)
                .tag("operation.category", "business")
                .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            LOG.info("Starting business operation: {} for {} with id: {}",
                       operationName, entityType, entityId);
            
            T result = operation.get();
            
            span.tag("operation.status", "success");
            LOG.info("Completed business operation: {} for {} with id: {}",
                       operationName, entityType, entityId);
            
            return result;
        } catch (Exception e) {
            span.tag("operation.status", "error");
            span.tag("error.message", e.getMessage());
            span.tag("error.type", e.getClass().getSimpleName());
            
            LOG.error("Failed business operation: {} for {} with id: {}",
                        operationName, entityType, entityId, e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    /**
     * Create a custom span for external service calls
     * 為外部服務調用創建自定義 span
     */
    public <T> T traceExternalCall(String serviceName, String operation, 
                                  java.util.function.Supplier<T> call) {
        Span span = tracer.nextSpan()
                .name("external-call")
                .tag("service.name", serviceName)
                .tag("operation", operation)
                .tag("span.kind", "client")
                .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            LOG.debug("Making external call to {} for operation: {}", serviceName, operation);
            
            T result = call.get();
            
            span.tag("call.status", "success");
            return result;
        } catch (Exception e) {
            span.tag("call.status", "error");
            span.tag("error.message", e.getMessage());
            span.tag("error.type", e.getClass().getSimpleName());
            
            LOG.error("External call failed to {} for operation: {}", serviceName, operation, e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    /**
     * Add custom attributes to current span
     * 向當前 span 添加自定義屬性
     */
    public void addSpanAttribute(String key, String value) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag(key, value);
        }
    }
    
    /**
     * Add business context to current span
     * 向當前 span 添加業務上下文
     */
    public void addBusinessContext(String userId, String tenantId, String requestId) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            if (userId != null) currentSpan.tag("user.id", userId);
            if (tenantId != null) currentSpan.tag("tenant.id", tenantId);
            if (requestId != null) currentSpan.tag("request.id", requestId);
        }
    }
}
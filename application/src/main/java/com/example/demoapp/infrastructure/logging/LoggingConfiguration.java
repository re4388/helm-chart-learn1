package com.example.demoapp.infrastructure.logging;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 日誌配置類
 * 啟用 AspectJ 自動代理以支持 AOP 日誌記錄
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingConfiguration {
    // 這個類主要用於啟用 AOP 功能
    // 所有的日誌邏輯都在 ApiLoggingAspect 中實現
}
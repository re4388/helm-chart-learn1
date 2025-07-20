package com.example.demoapp.infrastructure.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定義註解，用於控制 API 日誌記錄行為
 * 可以在方法或類級別使用
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiLog {
    
    /**
     * 是否記錄請求參數
     * @return true 記錄參數，false 不記錄
     */
    boolean logArgs() default true;
    
    /**
     * 是否記錄響應結果
     * @return true 記錄響應，false 不記錄
     */
    boolean logResult() default false;
    
    /**
     * 自定義日誌描述
     * @return 日誌描述
     */
    String description() default "";
    
    /**
     * 是否記錄執行時間
     * @return true 記錄執行時間，false 不記錄
     */
    boolean logExecutionTime() default true;
    
    /**
     * 慢查詢閾值（毫秒），超過此時間會記錄為 WARN 級別
     * @return 閾值毫秒數
     */
    long slowQueryThreshold() default 1000;
}
/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 日志切面 - AOP实现自动记录操作日志
 * Log Aspect - AOP implementation for automatic operation logging
 */

package top.zztech.ainote.runtime.aspect

import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import top.zztech.ainote.model.enums.RequestMethod
import top.zztech.ainote.runtime.annotation.LogOperation
import top.zztech.ainote.model.*
import top.zztech.ainote.runtime.utility.getCurrentAccountId
import java.util.UUID

/**
 * 日志操作切面
 * 
 * 拦截带有@LogOperation注解的方法，自动创建审计日志
 */
@Aspect
@Component
class LogAspect(
    private val sqlClient: KSqlClient
) {
    
    private val logger = LoggerFactory.getLogger(LogAspect::class.java)

    @Around("@annotation(top.zztech.ainote.runtime.annotation.LogOperation)")
    fun logOperation(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val logOperation = signature.method.getAnnotation(LogOperation::class.java)
        
        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        val request = requestAttributes?.request
        
        var result: Any? = null
        var exception: Exception? = null
        var responseStatus = 200
        
        try {
            // Execute the actual method
            result = joinPoint.proceed()
            responseStatus = 200
        } catch (e: Exception) {
            exception = e
            responseStatus = 500
            throw e
        } finally {
            try {
                // Create log entry
                createLogEntry(joinPoint, logOperation, request, responseStatus, exception)
            } catch (e: Exception) {
                logger.error("Failed to create log entry", e)
            }
        }
        
        return result
    }
    
    private fun createLogEntry(
        joinPoint: ProceedingJoinPoint,
        logOperation: LogOperation,
        request: HttpServletRequest?,
        responseStatus: Int,
        exception: Exception?
    ) {
        // Extract entity ID from method parameters or result
        val entityId = extractEntityId(joinPoint)
        
        // Extract user ID from SecurityContext
        val accountId = try {
            getCurrentAccountId()
        } catch (e: Exception) {
            null
        }
        
        // Build and save Log entity
        val logEntity = Log {
            this.id = UUID.randomUUID();
            this.accountId = accountId
            this.action = logOperation.action
            this.targetEntity = if (logOperation.entityType.isEmpty()) null else logOperation.entityType
            this.entityId = entityId
            this.ipAddress = if (logOperation.includeRequest && request != null) getClientIpAddress(request) else null
            this.userAgent = if (logOperation.includeRequest && request != null) request.getHeader("User-Agent") else null
            this.requestMethod = if (logOperation.includeRequest && request != null) convertRequestMethod(request.method) else null
            this.requestUrl = if (logOperation.includeRequest && request != null) request.requestURI else null
            this.responseStatus = if (logOperation.includeResponse) responseStatus else null
            this.errorMessage = exception?.message
        }
        
        sqlClient.save(logEntity)
        logger.info("Log created: action=${logOperation.action}, entityId=$entityId, accountId=$accountId")
    }
    
    /**
     * Extract entity ID from method parameters or result
     */
    private fun extractEntityId(joinPoint: ProceedingJoinPoint): UUID? {
        val args = joinPoint.args
        
        // Try to find ID from path variable (first UUID parameter)
        for (arg in args) {
            if (arg is UUID) {
                return arg
            }
        }
        
        // Try to extract ID from entity object with reflection
        for (arg in args) {
            try {
                val idField = arg?.javaClass?.getDeclaredField("id")
                idField?.isAccessible = true
                val id = idField?.get(arg)
                if (id is UUID) {
                    return id
                }
            } catch (e: Exception) {
                // Ignore and continue
            }
        }
        
        return null
    }
    
    /**
     * Get client IP address, handling proxies
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return when {
            !xForwardedFor.isNullOrEmpty() -> xForwardedFor.split(",")[0].trim()
            else -> request.remoteAddr
        }
    }
    
    /**
     * Convert HTTP method string to RequestMethod enum
     */
    private fun convertRequestMethod(method: String): RequestMethod? {
        return try {
            RequestMethod.valueOf(method.uppercase())
        } catch (e: Exception) {
            null
        }
    }
}

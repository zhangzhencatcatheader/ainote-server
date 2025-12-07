/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 日志操作注解 - 用于标记需要记录日志的接口方法
 * Log Operation Annotation - Used to mark interface methods that need logging
 */

package top.zztech.ainote.runtime.annotation

/**
 * 日志操作注解
 * 
 * 将此注解应用于控制器方法以自动记录操作
 * 
 * @param action 正在执行的操作（例如："CREATE_NOTE"、"DELETE_USER"）
 * @param entityType 被操作的实体类型（例如："Note"、"User"）
 * @param includeRequest 是否包含请求详情（IP、User-Agent、URL）
 * @param includeResponse 是否包含响应状态
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogOperation(
    val action: String,
    val entityType: String = "",
    val includeRequest: Boolean = true,
    val includeResponse: Boolean = true
)

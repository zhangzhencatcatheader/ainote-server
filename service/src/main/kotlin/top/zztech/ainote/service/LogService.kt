/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 日志服务 - 提供系统审计日志查询功能
 * Log Service - Provides system audit log query functionality
 */

package top.zztech.ainote.service

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import top.zztech.ainote.model.Log
import top.zztech.ainote.repository.LogRepository
import top.zztech.ainote.runtime.annotation.LogOperation
import java.time.LocalDateTime
import java.util.UUID

/**
 * 日志服务
 * 提供系统审计日志的查询和管理REST API接口
 */

/**
 * 日志服务 - 用于管理审计日志
 * 
 * 提供REST API用于查询和创建系统日志
 */
@RestController
@RequestMapping("/log")
@Transactional
class LogService(
    private val logRepository: LogRepository
) {

    /**
     * 获取所有日志
     * @return 日志列表
     */
    @LogOperation(action = "QUERY_ALL_LOGS", entityType = "Log", includeRequest = false)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun findAll(): List<Log> =
        logRepository.findAll()

    /**
     * 根据ID获取日志
     * @param id 日志ID
     * @return 日志对象，如果不存在则返回null
     */
    @LogOperation(action = "QUERY_LOG_BY_ID", entityType = "Log", includeRequest = false)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): Log? =
        logRepository.findById(id)

    /**
     * 根据用户ID获取日志
     * @param accountId 账户ID
     * @return 该用户的日志列表
     */
    @LogOperation(action = "QUERY_LOGS_BY_ACCOUNT", entityType = "Log", includeRequest = false)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/account/{accountId}")
    fun findByAccountId(@PathVariable accountId: UUID): List<Log> =
        logRepository.findByUserId(accountId)


    /**
     * 获取最新的日志（按创建时间降序）
     * @param limit 限制数量，默认10条
     * @return 最新的日志列表
     */
    @LogOperation(action = "QUERY_LATEST_LOGS", entityType = "Log", includeRequest = false)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/latest")
    fun findLatestLogs(@RequestParam(defaultValue = "10") limit: Int): List<Log> =
        logRepository.findLatestLogs(limit)


    /**
     * 根据ID删除日志
     * @param id 日志ID
     */
    @LogOperation(action = "DELETE_LOG", entityType = "Log", includeRequest = true)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) {
        logRepository.deleteById(id)
    }
}

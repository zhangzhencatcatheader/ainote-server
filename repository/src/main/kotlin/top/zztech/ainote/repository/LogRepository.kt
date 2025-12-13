/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 日志数据仓库 - 提供日志实体的数据访问功能
 * Log Repository - Provides data access functionality for Log entity
 */

package top.zztech.ainote.repository

import org.babyfish.jimmer.Page
import org.babyfish.jimmer.spring.repo.support.AbstractKotlinRepository
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.springframework.stereotype.Repository
import top.zztech.ainote.model.Log
import top.zztech.ainote.model.account
import top.zztech.ainote.model.createdTime
import top.zztech.ainote.model.id
import org.babyfish.jimmer.Specification
import java.util.UUID

/**
 * 日志数据仓库 - Log实体的Repository
 * 
 * 提供CRUD操作和审计日志的自定义查询
 */
@Repository
class LogRepository(
    sql: KSqlClient
) : AbstractKotlinRepository<Log, UUID>(sql) {
    
    /**
     * 根据用户ID查找日志
     * @param userId 用户ID
     * @return 该用户的日志列表（按创建时间降序）
     */
    fun findByUserId(userId: UUID): List<Log> =
        createQuery {
            where(table.account.id eq userId)
            orderBy(table.createdTime.desc())
            select(table)
        }.execute()

    /**
     * 查找最新的日志
     * @param limit 限制数量
     * @return 最新的日志列表（按创建时间降序）
     */
    fun findLatestLogs(limit: Int): List<Log> =
        createQuery {
            orderBy(table.createdTime.desc())
            select(table)
        }.limit(limit).execute()

    fun findAllPage(pageNum:Int,pageSize:Int,specification: Specification<Log>): Page<Log> =
        createQuery {
            where(specification)
            orderBy(table.createdTime.desc())
            select(table)
    }.fetchPage(pageNum,pageSize)
}

/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 静态文件数据仓库 - 提供文件实体的数据访问功能
 * Static File Repository - Provides data access functionality for StaticFile entity
 */

package top.zztech.ainote.repository

import org.babyfish.jimmer.spring.repo.support.AbstractKotlinRepository
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.springframework.stereotype.Repository
import top.zztech.ainote.model.StaticFile
import top.zztech.ainote.model.*
import top.zztech.ainote.model.enums.FileType
import java.util.UUID

/**
 * 静态文件数据仓库
 * 提供CRUD操作和文件的自定义查询
 */
@Repository
class StaticFileRepository(
    sql: KSqlClient
) : AbstractKotlinRepository<StaticFile, UUID>(sql) {

    /**
     * 根据上传者ID查找文件
     * @param uploaderId 上传者ID
     * @return 该用户上传的文件列表
     */
    fun findByUploaderId(uploaderId: UUID): List<StaticFile> =
        createQuery {
            where(table.uploaderId eq uploaderId)
            orderBy(table.createdTime.desc())
            select(table)
        }.execute()

    /**
     * 根据文件类型查找文件
     * @param fileType 文件类型
     * @return 指定类型的文件列表
     */
    fun findByFileType(fileType: FileType): List<StaticFile> =
        createQuery {
            where(table.fileType eq fileType)
            orderBy(table.createdTime.desc())
            select(table)
        }.execute()

    /**
     * 根据文件名模糊查找
     * @param keyword 关键词
     * @return 匹配的文件列表
     */
    fun searchByFileName(keyword: String): List<StaticFile> =
        createQuery {
            where(table.originalName like keyword)
            orderBy(table.createdTime.desc())
            select(table)
        }.execute()

    /**
     * 获取最新上传的文件
     * @param limit 限制数量
     * @return 最新的文件列表
     */
    fun findLatestFiles(limit: Int): List<StaticFile> =
        createQuery {
            orderBy(table.createdTime.desc())
            select(table)
        }.limit(limit).execute()
}

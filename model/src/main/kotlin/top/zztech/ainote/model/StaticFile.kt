/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 静态文件实体 - 文件管理
 * Static File Entity - File management
 */

package top.zztech.ainote.model

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.enums.FileType
import java.util.UUID

/**
 * 静态文件实体，用于文件管理
 */
@Entity
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface StaticFile : BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    /**
     * 文件名（存储在OSS中的名称）
     */
    val fileName: String

    /**
     * 原始文件名
     */
    val originalName: String

    /**
     * 文件大小（字节）
     */
    val fileSize: Long

    /**
     * 文件路径（OSS中的完整路径）
     */
    @Key
    val filePath: String

    /**
     * MIME类型
     */
    val mimeType: String?

    /**
     * 文件类型枚举
     */
    val fileType: FileType?

    /**
     * 上传者ID（关联到Account）
     */
    @IdView
    val uploaderId: UUID?

    /**
     * 上传者关系（可选）
     */
    @ManyToOne
    @JoinColumn(name = "uploader_id", foreignKeyType = ForeignKeyType.FAKE)
    val uploader: Account?
}
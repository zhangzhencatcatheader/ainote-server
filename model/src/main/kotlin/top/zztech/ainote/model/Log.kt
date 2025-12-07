/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * 日志实体 - 系统审计和操作跟踪
 * Log Entity - System audit and operation tracking
 */

package top.zztech.ainote.model

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.enums.RequestMethod
import java.util.UUID

/**
 * 日志实体，用于系统审计和操作跟踪
 */
@Entity
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface Log : BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    /**
     * 执行操作的账户
     */
    @IdView()
    val accountId: UUID?

    /**
     * 执行的操作（例如：CREATE、UPDATE、DELETE、LOGIN）
     */
    val action: String

    /**
     * 受操作影响的目标实体类型
     */
    @Column(name = "entity_type")
    val targetEntity: String?

    /**
     * 受操作影响的实体ID
     */
    @Column(name = "entity_id")
    val entityId: UUID?

    /**
     * 请求的IP地址
     */
    @Column(name = "ip_address")
    val ipAddress: String?

    /**
     * 请求的User Agent
     */
    @Column(name = "user_agent")
    val userAgent: String?

    /**
     * HTTP请求方法
     */
    @Column(name = "request_method")
    val requestMethod: RequestMethod?

    /**
     * 请求URL
     */
    @Column(name = "request_url")
    val requestUrl: String?

    /**
     * HTTP响应状态码
     */
    @Column(name = "response_status")
    val responseStatus: Int?

    /**
     * 错误信息（如有）
     */
    @Column(name = "error_message")
    val errorMessage: String?

    /**
     * 账户关系（可选）
     */
    @ManyToOne
    @JoinColumn(name = "account_id", foreignKeyType = ForeignKeyType.FAKE)
    val account: Account?
}
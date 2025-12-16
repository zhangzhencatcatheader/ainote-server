package top.zztech.ainote.model

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.common.PassiveEntity
import top.zztech.ainote.model.common.TenantAware
import java.util.UUID

/**
 * 台账模板实体
 * 
 * 用于定义各种台账的结构和字段，如安全生产巡查台账、设备维护台账等
 * 支持多租户，每个租户可以创建自己的台账模板
 */
@Entity
@Table(name = "ledger_template")
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface LedgerTemplate : PassiveEntity, BaseEntity, TenantAware {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    /**
     * 台账模板名称，如"安全生产巡查台账"、"设备维护台账"
     */
    @Key
    val name: String

    /**
     * 台账模板描述
     */
    val description: String?

    /**
     * 台账类型分类，用于分类管理，如"safety"、"equipment"、"maintenance"等
     */
    val category: String?

    /**
     * 模板版本号，用于追踪模板变更
     */
    @Default("1")
    val version: Int

    /**
     * 是否启用该模板
     */
    @Default("true")
    val enabled: Boolean

    /**
     * 图标名称或URL
     */
    @OneToOne()
    @JoinColumn(name = "icon_id")
    val icon: StaticFile?

    @OneToOne()
    @JoinColumn(name = "file_id")
    val file: StaticFile?
    /**
     * 主题颜色
     */
    val color: String?

    /**
     * 排序顺序
     */
    @Default("0")
    val sortOrder: Int

    /**
     * 模板的字段定义集合
     * 一对多关系：一个模板包含多个字段
     */
    @OneToMany(mappedBy = "template")
    val fields: List<LedgerTemplateField>

    /**
     * 基于此模板创建的记录
     */
    @OneToMany(mappedBy = "template")
    val records: List<LedgerRecord>
}

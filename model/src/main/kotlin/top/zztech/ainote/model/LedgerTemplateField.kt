package top.zztech.ainote.model

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.enums.FieldType
import java.math.BigDecimal
import java.util.UUID

/**
 * 台账模板字段实体
 * 
 * 定义台账模板的字段结构，包括字段类型、验证规则、显示控制等
 */
@Entity
@Table(name = "ledger_template_field")
@KeyUniqueConstraint(
    noMoreUniqueConstraints = true,
    isNullNotDistinct = true
)
interface LedgerTemplateField : BaseEntity {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    /**
     * 所属模板
     */
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val template: LedgerTemplate

    /**
     * 字段名称（英文标识），用于程序识别
     */
    @Key
    val fieldName: String

    /**
     * 字段标签（显示名称），用于界面展示
     */
    val fieldLabel: String

    /**
     * 字段类型：TEXT, TEXTAREA, NUMBER, DATE, DATETIME, SELECT等
     */
    val fieldType: FieldType

    /**
     * 字段选项（JSON格式），用于SELECT、MULTISELECT等类型
     * 例如：["选项1", "选项2", "选项3"]
     */
    val fieldOptions: String?

    /**
     * 默认值
     */
    val defaultValue: String?

    /**
     * 占位符提示
     */
    val placeholder: String?

    /**
     * 帮助说明文本
     */
    val helpText: String?

    // ========== 验证规则 ==========

    /**
     * 是否必填
     */
    @Default("false")
    val required: Boolean

    /**
     * 最小长度（用于文本类型）
     */
    val minLength: Int?

    /**
     * 最大长度（用于文本类型）
     */
    val maxLength: Int?

    /**
     * 最小值（用于数字类型）
     */
    val minValue: BigDecimal?

    /**
     * 最大值（用于数字类型）
     */
    val maxValue: BigDecimal?

    /**
     * 正则表达式验证模式
     */
    val pattern: String?

    // ========== 显示控制 ==========

    /**
     * 字段排序顺序
     */
    @Default("0")
    val sortOrder: Int

    /**
     * 字段宽度（如"50%"、"200px"）
     */
    val width: String?

    /**
     * 是否可见
     */
    @Default("true")
    val visible: Boolean

    /**
     * 是否可编辑
     */
    @Default("true")
    val editable: Boolean

    /**
     * 是否可搜索
     */
    @Default("false")
    val searchable: Boolean

}

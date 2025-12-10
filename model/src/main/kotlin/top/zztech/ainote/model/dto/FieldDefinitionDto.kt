package top.zztech.ainote.model.dto

import org.babyfish.jimmer.sql.Serialized
import top.zztech.ainote.model.enums.FieldType
import java.math.BigDecimal

/**
 * 台账字段定义的 JSON DTO
 *
 * 用于在 LedgerRecordValue 中存储字段的完整定义信息
 */
@Serialized
data class FieldDefinitionDto(
    /**
     * 字段名称（英文标识），用于程序识别
     */
    val fieldName: String,

    /**
     * 字段标签（显示名称），用于界面展示
     */
    val fieldLabel: String,

    /**
     * 字段类型：TEXT, TEXTAREA, NUMBER, DATE, DATETIME, SELECT等
     */
    val fieldType: FieldType,

    /**
     * 字段选项，用于SELECT、MULTISELECT等类型
     * 例如：["选项1", "选项2", "选项3"]
     */
    val fieldOptions: List<String>? = null,

    /**
     * 默认值
     */
    val defaultValue: String? = null,

    /**
     * 占位符提示
     */
    val placeholder: String? = null,

    /**
     * 帮助说明文本
     */
    val helpText: String? = null,

    // ========== 验证规则 ==========

    /**
     * 是否必填
     */
    val required: Boolean = false,

    /**
     * 最小长度（用于文本类型）
     */
    val minLength: Int? = null,

    /**
     * 最大长度（用于文本类型）
     */
    val maxLength: Int? = null,

    /**
     * 最小值（用于数字类型）
     */
    val minValue: BigDecimal? = null,

    /**
     * 最大值（用于数字类型）
     */
    val maxValue: BigDecimal? = null,

    /**
     * 正则表达式验证模式
     */
    val pattern: String? = null,

    // ========== 显示控制 ==========

    /**
     * 字段排序顺序
     */
    val sortOrder: Int = 0,

    /**
     * 字段宽度（如"50%"、"200px"）
     */
    val width: String? = null,

    /**
     * 是否可见
     */
    val visible: Boolean = true,

    /**
     * 是否可编辑
     */
    val editable: Boolean = true,

    /**
     * 是否可搜索
     */
    val searchable: Boolean = false
)
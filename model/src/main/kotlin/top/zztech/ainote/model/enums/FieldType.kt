package top.zztech.ainote.model.enums

import org.babyfish.jimmer.sql.EnumType

/**
 * 台账字段类型枚举
 */
@EnumType(EnumType.Strategy.NAME)
enum class FieldType {
    /**
     * 文本字段
     */
    TEXT,
    
    /**
     * 长文本/富文本字段
     */
    TEXTAREA,
    
    /**
     * 数字字段
     */
    NUMBER,
    
    /**
     * 整数字段
     */
    INTEGER,
    
    /**
     * 小数字段
     */
    DECIMAL,
    
    /**
     * 日期字段 (YYYY-MM-DD)
     */
    DATE,
    
    /**
     * 日期时间字段 (YYYY-MM-DD HH:mm:ss)
     */
    DATETIME,
    
    /**
     * 时间字段 (HH:mm:ss)
     */
    TIME,
    
    /**
     * 布尔值/复选框字段
     */
    BOOLEAN,
    
    /**
     * 单选字段 (下拉列表)
     */
    SELECT,
    
    /**
     * 多选字段 (复选框组)
     */
    MULTISELECT,
    
    /**
     * 附件/文件字段
     */
    FILE,
    
    /**
     * 邮箱字段
     */
    EMAIL,
    
    /**
     * 电话号码字段
     */
    PHONE,
    
    /**
     * URL字段
     */
    URL
}

package top.zztech.ainote.model.enums

import org.babyfish.jimmer.sql.EnumType

/**
 * 台账记录状态枚举
 */
@EnumType(EnumType.Strategy.NAME)
enum class LedgerRecordStatus {
    /**
     * 草稿
     */
    DRAFT,
    
    /**
     * 已提交
     */
    SUBMITTED,
    
    /**
     * 已审批
     */
    APPROVED,
    
    /**
     * 已驳回
     */
    REJECTED
}

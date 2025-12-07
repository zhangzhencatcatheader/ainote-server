package top.zztech.ainote.model.enums

import org.babyfish.jimmer.sql.EnumType

@EnumType(EnumType.Strategy.ORDINAL)
enum class CompanyStatus {
    /**
     * 未激活 - 公司未激活/已禁用
     */
    INACTIVE,
    
    /**
     * 活跃 - 公司正常运营
     */
    ACTIVE,
    
    /**
     * 已暂停 - 公司已被暂停
     */
    SUSPENDED,
    
    /**
     * 待审核 - 公司待审核/待验证
     */
    PENDING,
    
    /**
     * 已删除 - 公司已被标记为删除
     */
    DELETED
}
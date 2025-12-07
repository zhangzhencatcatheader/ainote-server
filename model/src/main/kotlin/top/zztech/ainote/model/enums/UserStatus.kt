package top.zztech.ainote.model.enums

import org.babyfish.jimmer.sql.EnumType

@EnumType(EnumType.Strategy.ORDINAL)
enum class UserStatus {
    /**
     * 未激活 - 用户账号未激活/已禁用
     */
    INACTIVE,
    
    /**
     * 活跃 - 用户账号正常，可以登录
     */
    ACTIVE,
    
    /**
     * 已锁定 - 用户账号被锁定（如登录失败次数过多）
     */
    LOCKED,
    
    /**
     * 待审核 - 用户账号待审核/待激活
     */
    PENDING,
    
    /**
     * 已删除 - 用户账号已被标记为删除
     */
    DELETED
}
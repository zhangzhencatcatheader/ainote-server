package top.zztech.ainote.model.enums

import org.babyfish.jimmer.sql.EnumType

@EnumType(EnumType.Strategy.NAME)
enum class RoleEnum {
    ADMIN,
    COMPANYADMIN,
    USER
}
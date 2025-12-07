package top.zztech.ainote.model.enums

import org.babyfish.jimmer.sql.EnumType

@EnumType(EnumType.Strategy.NAME)
enum class RequestMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS
}
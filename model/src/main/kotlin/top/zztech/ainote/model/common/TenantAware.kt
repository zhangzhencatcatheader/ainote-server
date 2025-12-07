package top.zztech.ainote.model.common

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface TenantAware {

    /**
     * The tenant to which the current object belongs.
     *
     * In this example, this property is not
     * explicitly modified by business code,
     * but is automatically modified by `DraftInterceptor`
     */
    val tenant: String
}

package top.zztech.ainote.runtime.filter

import top.zztech.ainote.runtime.TenantProvider
import top.zztech.ainote.model.common.TenantAware
import top.zztech.ainote.model.common.tenant
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.filter.KFilter
import org.babyfish.jimmer.sql.kt.filter.KFilterArgs
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@ConditionalOnMissingBean(name = ["tenantFilterForCacheMode"])
@Component
open class TenantFilterForNonCacheMode(
    protected val tenantProvider: TenantProvider
) : KFilter<TenantAware> {

    override fun filter(args: KFilterArgs<TenantAware>) {
        tenantProvider.tenant?.let {
            args.apply {
                where(table.tenant eq it)
            }
        }
    }
}

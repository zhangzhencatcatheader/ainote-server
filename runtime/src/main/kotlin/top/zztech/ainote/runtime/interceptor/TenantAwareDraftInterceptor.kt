package top.zztech.ainote.runtime.interceptor

import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import top.zztech.ainote.model.common.TenantAware
import top.zztech.ainote.model.common.TenantAwareDraft
import top.zztech.ainote.runtime.TenantProvider
import org.springframework.stereotype.Component

@Component
class TenantAwareDraftInterceptor(
    private val tenantProvider: TenantProvider
) : DraftInterceptor<TenantAware, TenantAwareDraft> {

    override fun beforeSave(draft: TenantAwareDraft, original: TenantAware?) {
        if (!isLoaded(draft, TenantAware::tenant)) {
            draft.tenant = tenantProvider.tenant
                ?: error("Cannot save tenant aware entity when tenant is unknown")
        }
    }
}

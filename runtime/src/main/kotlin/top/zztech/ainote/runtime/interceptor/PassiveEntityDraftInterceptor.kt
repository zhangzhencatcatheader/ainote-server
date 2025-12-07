package top.zztech.ainote.runtime.interceptor

import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import top.zztech.ainote.model.common.PassiveEntity
import top.zztech.ainote.model.common.PassiveEntityDraft
import top.zztech.ainote.runtime.dto.IdUserDetails

@Component
class PassiveEntityDraftInterceptor : DraftInterceptor<PassiveEntity, PassiveEntityDraft> {
    override fun beforeSave(draft: PassiveEntityDraft, original: PassiveEntity?) {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as IdUserDetails
        if (!isLoaded(draft, PassiveEntity::account)) {
            draft.account {
                id = userDetails.id
            }
        }

        if (original === null) {
            if (!isLoaded(draft, PassiveEntity::account)) {
                draft.account {
                    id = userDetails.id
                }
            }
        }
    }
}
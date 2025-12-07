package top.zztech.ainote.runtime.interceptor

import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import top.zztech.ainote.model.common.BaseEntity
import top.zztech.ainote.model.common.BaseEntityDraft
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BaseEntityDraftInterceptor : DraftInterceptor<BaseEntity, BaseEntityDraft> {

    override fun beforeSave(draft: BaseEntityDraft, original: BaseEntity?) {
        if (!isLoaded(draft, BaseEntity::modifiedTime)) {
            draft.modifiedTime = LocalDateTime.now()
        }
        // `original === null` means `INSERT`
        if (original === null && !isLoaded(draft, BaseEntity::createdTime)) {
            draft.createdTime = LocalDateTime.now()
        }
    }
}

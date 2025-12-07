package top.zztech.ainote.runtime

import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class TenantProvider {

    val tenant: String?
        get() = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
            ?.request
            ?.getHeader("tenant")
            ?.takeIf { it.isNotEmpty() }
}

package top.zztech.ainote.runtime.utility

import org.springframework.security.core.context.SecurityContextHolder
import top.zztech.ainote.runtime.dto.IdUserDetails
import java.util.*

fun getCurrentAccountId(): UUID? {
    val authentication = SecurityContextHolder.getContext().authentication
    return if (authentication?.principal is IdUserDetails) {
        (authentication.principal as IdUserDetails).id
    } else {
        null
    }
}

fun isCurrentUserModerator(): Boolean {
    val authentication = SecurityContextHolder.getContext().authentication
    return authentication?.authorities?.any { it.authority == "MODERATOR" } == true
} 
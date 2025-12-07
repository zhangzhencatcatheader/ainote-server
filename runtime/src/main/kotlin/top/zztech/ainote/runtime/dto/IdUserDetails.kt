package top.zztech.ainote.runtime.dto

import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

abstract class IdUserDetails(
    val id: UUID,
) : UserDetails
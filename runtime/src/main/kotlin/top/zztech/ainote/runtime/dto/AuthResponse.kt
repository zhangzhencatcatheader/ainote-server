package top.zztech.ainote.runtime.dto
import java.util.*

data class AuthResponse(
    val id: UUID,
    val token: String,
    val role: String
)

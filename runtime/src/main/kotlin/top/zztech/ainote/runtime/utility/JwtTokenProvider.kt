package top.zztech.ainote.runtime.utility

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @param:Value($$"${jwt.secret}") val jwtSecret: String,
    @param:Value($$"${jwt.expiration}") val jwtExpirationMs: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    fun generateToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder().subject(username).issuedAt(now).expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUsernameFromJWT(token: String): String {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parse(token)
            .getPayload() as Claims
        return claims.subject
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            return true
        } catch (e: JwtException) {
            return false
        } catch (e: IllegalArgumentException) {
            return false
        }
    }
}
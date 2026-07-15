package uz.mkb.auth.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil (
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.access-token-expiration-ms}") val accessTokenExpirationMs: Long,
    @Value("\${app.jwt.refresh-token-expiration-ms}") val refreshTokenExpirationMs: Long,
) {

    private val signingKey: SecretKey by lazy {
        val keyBytes = try {
            Decoders.BASE64.decode(secret)
        } catch (e: Exception) {
            secret.toByteArray()
        }

        Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateAccessToken(username: String, role: String): String =
        buildToken(username, mapOf("role" to role, "type" to "access"), accessTokenExpirationMs)

    fun generateRefreshToken(username: String): String =
        buildToken(username, mapOf("type" to "refresh"), refreshTokenExpirationMs)

    private fun buildToken(subject: String, claims: Map<String, Any>, expirationMs: Long): String {
        val now = Date()
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(signingKey)
            .compact()
    }

    fun extractUsername(token: String): String = extractAllClaims(token).subject

    fun isTokenValid(token: String, expectedType: String): Boolean = try {
        val claims = extractAllClaims(token)
        !claims.expiration.before(Date()) && claims["type"] as? String == expectedType
    } catch (ex: Exception) {
        false
    }

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).payload

}
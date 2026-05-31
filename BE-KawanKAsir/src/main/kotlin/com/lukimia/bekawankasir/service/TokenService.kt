package com.lukimia.bekawankasir.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class TokenService(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.access-token-minutes}") private val accessMinutes: Long,
    @Value("\${app.jwt.refresh-token-days}") private val refreshDays: Long,
) {
    private val mapper = jacksonObjectMapper()
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun accessToken(userId: UUID, role: String) = token(userId, role, Instant.now().plusSeconds(accessMinutes * 60), "access")
    fun refreshToken(userId: UUID, role: String) = token(userId, role, Instant.now().plusSeconds(refreshDays * 24 * 3600), "refresh")

    private fun token(userId: UUID, role: String, expiresAt: Instant, type: String): String {
        val header = mapOf("alg" to "HS256", "typ" to "JWT")
        val payload = mapOf("sub" to userId.toString(), "role" to role, "type" to type, "iat" to Instant.now().epochSecond, "exp" to expiresAt.epochSecond)
        val unsigned = encode(header) + "." + encode(payload)
        return unsigned + "." + sign(unsigned)
    }

    fun parse(token: String): TokenClaims? = runCatching {
        val parts = token.split('.')
        require(parts.size == 3)
        val unsigned = parts[0] + "." + parts[1]
        require(sign(unsigned) == parts[2])
        val payload: Map<String, Any> = mapper.readValue(String(decoder.decode(parts[1]), StandardCharsets.UTF_8))
        val exp = (payload["exp"] as Number).toLong()
        require(Instant.now().epochSecond < exp)
        TokenClaims(UUID.fromString(payload["sub"].toString()), payload["role"].toString(), payload["type"].toString())
    }.getOrNull()

    private fun encode(value: Any): String = encoder.encodeToString(mapper.writeValueAsBytes(value))
    private fun sign(value: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        return encoder.encodeToString(mac.doFinal(value.toByteArray(StandardCharsets.UTF_8)))
    }
}

data class TokenClaims(val userId: UUID, val role: String, val type: String)

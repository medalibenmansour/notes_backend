package com.daly.notes_backend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

private const val CLAIM_TYPE_KEY = "type"
private const val TOKEN_TYPE_ACCESS = "access"
private const val TOKEN_TYPE_REFRESH = "refresh"
const val BEARER = "Bearer "

@Service
class JwtService(
    @Value("\${jwt.secret}") private val jwtSecret: String
) {
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))
    private val accessTokenValidityMs = 15L * 60L * 1000L
    val refreshTokenValidityMs = 30L * 24L * 60L * 1000L

    private fun generateToken(
        userId: String,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)

        return Jwts.builder()
            .subject(userId)
            .claim(CLAIM_TYPE_KEY, type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String): String {
        return generateToken(userId, TOKEN_TYPE_ACCESS, accessTokenValidityMs)
    }

    fun generateRefreshToken(userId: String): String {
        return generateToken(userId, TOKEN_TYPE_REFRESH, refreshTokenValidityMs)
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims[CLAIM_TYPE_KEY] as? String ?: return false
        return tokenType == TOKEN_TYPE_ACCESS
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims[CLAIM_TYPE_KEY] as? String ?: return false
        return tokenType == TOKEN_TYPE_REFRESH
    }

    fun getUserIdFromToken(token: String): String {
        val claims = parseAllClaims(token) ?: throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid token.")
        return claims.subject
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith(BEARER)) {
            token.removePrefix(BEARER)
        } else token

        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}
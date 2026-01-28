package com.nbang.nbangapi.support.security

import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))
    }

    fun createAccessToken(memberId: Long): String {
        return createToken(memberId, jwtProperties.accessTokenExpiration)
    }

    fun createRefreshToken(memberId: Long): String {
        return createToken(memberId, jwtProperties.refreshTokenExpiration)
    }

    private fun createToken(memberId: Long, expiration: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getMemberIdFromToken(token: String): Long {
        val claims = parseClaims(token)
        return claims.subject.toLong()
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseClaims(token)
            true
        } catch (e: ExpiredJwtException) {
            log.debug("Expired JWT token: {}", e.message)
            false
        } catch (e: JwtException) {
            log.debug("Invalid JWT token: {}", e.message)
            false
        }
    }

    fun validateTokenOrThrow(token: String) {
        try {
            parseClaims(token)
        } catch (e: ExpiredJwtException) {
            throw CoreException(ErrorType.EXPIRED_TOKEN)
        } catch (e: JwtException) {
            throw CoreException(ErrorType.INVALID_TOKEN)
        }
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}

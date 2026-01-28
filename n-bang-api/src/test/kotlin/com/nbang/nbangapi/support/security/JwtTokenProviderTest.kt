package com.nbang.nbangapi.support.security

import com.nbang.nbangapi.support.IntegrationTest
import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.util.*

class JwtTokenProviderTest @Autowired constructor(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${jwt.secret}") private val secret: String,
) : IntegrationTest() {

    private fun createExpiredToken(memberId: Long): String {
        val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
        val now = Date()
        val expiredDate = Date(now.time - 1000) // 1초 전에 만료

        return Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(Date(now.time - 10000))
            .expiration(expiredDate)
            .signWith(key)
            .compact()
    }

    @Test
    @DisplayName("액세스 토큰을 생성할 수 있다")
    fun createAccessToken() {
        // given
        val memberId = 1L

        // when
        val token = jwtTokenProvider.createAccessToken(memberId)

        // then
        assertThat(token).isNotBlank()
        assertThat(jwtTokenProvider.validateToken(token)).isTrue()
    }

    @Test
    @DisplayName("리프레시 토큰을 생성할 수 있다")
    fun createRefreshToken() {
        // given
        val memberId = 1L

        // when
        val token = jwtTokenProvider.createRefreshToken(memberId)

        // then
        assertThat(token).isNotBlank()
        assertThat(jwtTokenProvider.validateToken(token)).isTrue()
    }

    @Test
    @DisplayName("토큰에서 회원 ID를 추출할 수 있다")
    fun getMemberIdFromToken() {
        // given
        val memberId = 123L
        val token = jwtTokenProvider.createAccessToken(memberId)

        // when
        val extractedId = jwtTokenProvider.getMemberIdFromToken(token)

        // then
        assertThat(extractedId).isEqualTo(memberId)
    }

    @Test
    @DisplayName("유효한 토큰을 검증할 수 있다")
    fun validateValidToken() {
        // given
        val token = jwtTokenProvider.createAccessToken(1L)

        // when
        val isValid = jwtTokenProvider.validateToken(token)

        // then
        assertThat(isValid).isTrue()
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 시 false를 반환한다")
    fun validateInvalidToken() {
        // given
        val invalidToken = "invalid.token.here"

        // when
        val isValid = jwtTokenProvider.validateToken(invalidToken)

        // then
        assertThat(isValid).isFalse()
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 validateTokenOrThrow 호출 시 예외가 발생한다")
    fun validateTokenOrThrowWithInvalidToken() {
        // given
        val invalidToken = "invalid.token.here"

        // when & then
        assertThatThrownBy { jwtTokenProvider.validateTokenOrThrow(invalidToken) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.INVALID_TOKEN)
    }

    @Test
    @DisplayName("유효한 토큰으로 validateTokenOrThrow 호출 시 예외가 발생하지 않는다")
    fun validateTokenOrThrowWithValidToken() {
        // given
        val validToken = jwtTokenProvider.createAccessToken(1L)

        // when & then (no exception)
        jwtTokenProvider.validateTokenOrThrow(validToken)
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 false를 반환한다")
    fun validateExpiredToken() {
        // given
        val expiredToken = createExpiredToken(1L)

        // when
        val isValid = jwtTokenProvider.validateToken(expiredToken)

        // then
        assertThat(isValid).isFalse()
    }

    @Test
    @DisplayName("만료된 토큰으로 validateTokenOrThrow 호출 시 EXPIRED_TOKEN 예외가 발생한다")
    fun validateExpiredTokenThrows() {
        // given
        val expiredToken = createExpiredToken(1L)

        // when & then
        assertThatThrownBy { jwtTokenProvider.validateTokenOrThrow(expiredToken) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.EXPIRED_TOKEN)
    }

    @Test
    @DisplayName("형식이 잘못된 토큰 검증 시 false를 반환한다")
    fun validateMalformedToken() {
        // given
        val malformedToken = "not.a.valid.jwt.token"

        // when
        val isValid = jwtTokenProvider.validateToken(malformedToken)

        // then
        assertThat(isValid).isFalse()
    }

    @Test
    @DisplayName("변조된 토큰 검증 시 false를 반환한다")
    fun validateTamperedToken() {
        // given
        val validToken = jwtTokenProvider.createAccessToken(1L)
        val tamperedToken = validToken.dropLast(5) + "xxxxx" // 서명 부분 변조

        // when
        val isValid = jwtTokenProvider.validateToken(tamperedToken)

        // then
        assertThat(isValid).isFalse()
    }
}

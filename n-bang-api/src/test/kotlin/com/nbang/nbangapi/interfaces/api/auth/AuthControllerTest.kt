package com.nbang.nbangapi.interfaces.api.auth

import com.nbang.nbangapi.application.auth.request.TokenRefreshRequest
import com.nbang.nbangapi.domain.member.Member
import com.nbang.nbangapi.domain.member.MemberRepository
import com.nbang.nbangapi.domain.member.OAuthProvider
import com.nbang.nbangapi.domain.member.Role
import com.nbang.nbangapi.support.E2ETest
import com.nbang.nbangapi.support.security.JwtTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AuthControllerTest : E2ETest() {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var testMember: Member
    private lateinit var testRefreshToken: String

    @BeforeEach
    fun setUpMember() {
        testMember = memberRepository.save(
            Member(
                email = "auth-test@example.com",
                nickname = "인증테스트",
                profileImage = "https://example.com/profile.jpg",
                provider = OAuthProvider.GOOGLE,
                providerId = "google-auth-test",
                role = Role.USER
            )
        )
        testRefreshToken = jwtTokenProvider.createRefreshToken(testMember.id!!)
    }

    @Test
    @DisplayName("리프레시 토큰으로 새 토큰을 발급받을 수 있다")
    fun refreshToken() {
        // given
        val request = TokenRefreshRequest(refreshToken = testRefreshToken)

        // when & then
        performPost("/api/v1/auth/refresh", request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").isNumber)
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 요청 시 401을 반환한다")
    fun refreshTokenWithInvalidToken() {
        // given
        val request = TokenRefreshRequest(refreshToken = "invalid-token")

        // when & then
        performPost("/api/v1/auth/refresh", request)
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.meta.result").value("FAIL"))
            .andExpect(jsonPath("$.meta.errorCode").value("INVALID_TOKEN"))
    }

    @Test
    @DisplayName("내 정보를 조회할 수 있다")
    fun getMe() {
        // when & then
        performGet("/api/v1/auth/me")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").exists())
            .andExpect(jsonPath("$.nickname").exists())
            .andExpect(jsonPath("$.provider").exists())
    }
}

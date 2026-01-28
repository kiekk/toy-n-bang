package com.nbang.nbangapi.application.auth

import com.nbang.nbangapi.application.auth.request.TokenRefreshRequest
import com.nbang.nbangapi.domain.member.Member
import com.nbang.nbangapi.domain.member.MemberRepository
import com.nbang.nbangapi.domain.member.OAuthProvider
import com.nbang.nbangapi.domain.member.Role
import com.nbang.nbangapi.support.IntegrationTest
import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import com.nbang.nbangapi.support.security.JwtTokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AuthFacadeTest @Autowired constructor(
    private val authFacade: AuthFacade,
    private val memberRepository: MemberRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) : IntegrationTest() {

    private lateinit var testMember: Member
    private lateinit var validRefreshToken: String

    @BeforeEach
    fun setUp() {
        testMember = memberRepository.save(
            Member(
                email = "test@example.com",
                nickname = "테스트유저",
                profileImage = "https://example.com/image.jpg",
                provider = OAuthProvider.GOOGLE,
                providerId = "google-test-123",
                role = Role.USER
            )
        )
        validRefreshToken = jwtTokenProvider.createRefreshToken(testMember.id!!)
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 새 토큰을 발급받을 수 있다")
    fun refreshTokenSuccess() {
        // given
        val request = TokenRefreshRequest(refreshToken = validRefreshToken)

        // when
        val response = authFacade.refreshToken(request)

        // then
        assertThat(response.accessToken).isNotBlank()
        assertThat(response.refreshToken).isNotBlank()
        assertThat(response.tokenType).isEqualTo("Bearer")
        assertThat(response.expiresIn).isGreaterThan(0)
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 요청 시 예외가 발생한다")
    fun refreshTokenWithInvalidToken() {
        // given
        val request = TokenRefreshRequest(refreshToken = "invalid-token")

        // when & then
        assertThatThrownBy { authFacade.refreshToken(request) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.INVALID_TOKEN)
    }

    @Test
    @DisplayName("내 정보를 조회할 수 있다")
    fun getMe() {
        // when
        val response = authFacade.getMe(testMember.id!!)

        // then
        assertThat(response.id).isEqualTo(testMember.id)
        assertThat(response.email).isEqualTo("test@example.com")
        assertThat(response.nickname).isEqualTo("테스트유저")
        assertThat(response.profileImage).isEqualTo("https://example.com/image.jpg")
        assertThat(response.provider).isEqualTo("GOOGLE")
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외가 발생한다")
    fun getMeNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { authFacade.getMe(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.MEMBER_NOT_FOUND)
    }
}

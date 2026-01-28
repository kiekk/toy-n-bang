package com.nbang.nbangapi.domain.member

import com.nbang.nbangapi.support.IntegrationTest
import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MemberServiceTest @Autowired constructor(
    private val memberService: MemberService,
    private val memberRepository: MemberRepository,
) : IntegrationTest() {

    @Test
    @DisplayName("ID로 회원을 조회할 수 있다")
    fun getById() {
        // given
        val saved = memberRepository.save(
            Member(
                email = "test@example.com",
                nickname = "테스트",
                profileImage = null,
                provider = OAuthProvider.GOOGLE,
                providerId = "google-123",
                role = Role.USER
            )
        )

        // when
        val found = memberService.getById(saved.id!!)

        // then
        assertThat(found.id).isEqualTo(saved.id)
        assertThat(found.email).isEqualTo("test@example.com")
        assertThat(found.nickname).isEqualTo("테스트")
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외가 발생한다")
    fun getByIdNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { memberService.getById(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.MEMBER_NOT_FOUND)
    }

    @Test
    @DisplayName("신규 회원을 생성할 수 있다")
    fun createNewMember() {
        // when
        val member = memberService.createOrUpdate(
            email = "new@example.com",
            nickname = "신규회원",
            profileImage = "https://example.com/image.jpg",
            provider = OAuthProvider.KAKAO,
            providerId = "kakao-456"
        )

        // then
        assertThat(member.id).isNotNull()
        assertThat(member.email).isEqualTo("new@example.com")
        assertThat(member.nickname).isEqualTo("신규회원")
        assertThat(member.profileImage).isEqualTo("https://example.com/image.jpg")
        assertThat(member.provider).isEqualTo(OAuthProvider.KAKAO)
        assertThat(member.providerId).isEqualTo("kakao-456")
    }

    @Test
    @DisplayName("기존 회원 정보를 업데이트할 수 있다")
    fun updateExistingMember() {
        // given
        memberRepository.save(
            Member(
                email = "existing@example.com",
                nickname = "기존닉네임",
                profileImage = null,
                provider = OAuthProvider.GOOGLE,
                providerId = "google-existing",
                role = Role.USER
            )
        )

        // when
        val updated = memberService.createOrUpdate(
            email = "existing@example.com",
            nickname = "수정된닉네임",
            profileImage = "https://example.com/new-image.jpg",
            provider = OAuthProvider.GOOGLE,
            providerId = "google-existing"
        )

        // then
        assertThat(updated.nickname).isEqualTo("수정된닉네임")
        assertThat(updated.profileImage).isEqualTo("https://example.com/new-image.jpg")
    }
}

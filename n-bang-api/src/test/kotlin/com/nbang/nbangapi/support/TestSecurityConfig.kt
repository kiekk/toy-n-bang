package com.nbang.nbangapi.support

import com.nbang.nbangapi.domain.member.Member
import com.nbang.nbangapi.domain.member.MemberRepository
import com.nbang.nbangapi.domain.member.OAuthProvider
import com.nbang.nbangapi.domain.member.Role
import com.nbang.nbangapi.support.security.JwtTokenProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestSecurityConfig {

    @Bean
    @Primary
    fun testMemberSetup(memberRepository: MemberRepository): TestMemberSetup {
        return TestMemberSetup(memberRepository)
    }
}

class TestMemberSetup(
    private val memberRepository: MemberRepository
) {
    fun createTestMember(): Member {
        return memberRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "test-user-id")
            ?: memberRepository.save(
                Member(
                    email = "test@example.com",
                    nickname = "테스트유저",
                    profileImage = null,
                    provider = OAuthProvider.GOOGLE,
                    providerId = "test-user-id",
                    role = Role.USER
                )
            )
    }
}

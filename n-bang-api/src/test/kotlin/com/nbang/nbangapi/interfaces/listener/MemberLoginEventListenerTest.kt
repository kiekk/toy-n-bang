package com.nbang.nbangapi.interfaces.listener

import com.nbang.nbangapi.application.auth.event.MemberLoginEvent
import com.nbang.nbangapi.domain.member.Member
import com.nbang.nbangapi.domain.member.MemberLoginHistoryRepository
import com.nbang.nbangapi.domain.member.MemberRepository
import com.nbang.nbangapi.domain.member.OAuthProvider
import com.nbang.nbangapi.domain.member.Role
import com.nbang.nbangapi.support.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import java.util.concurrent.TimeUnit

class MemberLoginEventListenerTest @Autowired constructor(
    private val eventPublisher: ApplicationEventPublisher,
    private val memberRepository: MemberRepository,
    private val memberLoginHistoryRepository: MemberLoginHistoryRepository,
) : IntegrationTest() {

    @Test
    @DisplayName("로그인 이벤트가 발행되면 로그인 이력이 저장된다")
    fun handleMemberLoginEvent() {
        // given
        val member = memberRepository.save(
            Member(
                email = "test@example.com",
                nickname = "테스트",
                profileImage = null,
                provider = OAuthProvider.GOOGLE,
                providerId = "google-123",
                role = Role.USER
            )
        )

        val event = MemberLoginEvent(
            memberId = member.id!!,
            provider = OAuthProvider.GOOGLE,
            ipAddress = "127.0.0.1",
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
        )

        // when
        eventPublisher.publishEvent(event)

        // @Async로 비동기 처리되므로 잠시 대기
        TimeUnit.MILLISECONDS.sleep(500)

        // then
        val histories = memberLoginHistoryRepository.findByMemberId(member.id!!)
        assertThat(histories).hasSize(1)
        assertThat(histories[0].memberId).isEqualTo(member.id)
        assertThat(histories[0].provider).isEqualTo(OAuthProvider.GOOGLE)
        assertThat(histories[0].ipAddress).isEqualTo("127.0.0.1")
        assertThat(histories[0].userAgent).isEqualTo("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
    }

    @Test
    @DisplayName("동일 회원의 여러 로그인 이벤트가 발행되면 각각 이력이 저장된다")
    fun handleMultipleLoginEvents() {
        // given
        val member = memberRepository.save(
            Member(
                email = "test@example.com",
                nickname = "테스트",
                profileImage = null,
                provider = OAuthProvider.KAKAO,
                providerId = "kakao-123",
                role = Role.USER
            )
        )

        // when
        repeat(3) {
            eventPublisher.publishEvent(
                MemberLoginEvent(
                    memberId = member.id!!,
                    provider = OAuthProvider.KAKAO,
                    ipAddress = "192.168.0.$it",
                    userAgent = null,
                )
            )
        }

        TimeUnit.MILLISECONDS.sleep(500)

        // then
        val histories = memberLoginHistoryRepository.findByMemberId(member.id!!)
        assertThat(histories).hasSize(3)
    }
}

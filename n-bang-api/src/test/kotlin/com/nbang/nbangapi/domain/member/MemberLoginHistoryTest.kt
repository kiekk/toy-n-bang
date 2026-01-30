package com.nbang.nbangapi.domain.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MemberLoginHistoryTest {

    @Test
    @DisplayName("로그인 이력을 생성할 수 있다")
    fun create() {
        // when
        val history = MemberLoginHistory.create(
            memberId = 1L,
            provider = OAuthProvider.GOOGLE,
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0",
        )

        // then
        assertThat(history.memberId).isEqualTo(1L)
        assertThat(history.provider).isEqualTo(OAuthProvider.GOOGLE)
        assertThat(history.ipAddress).isEqualTo("192.168.1.1")
        assertThat(history.userAgent).isEqualTo("Mozilla/5.0")
        assertThat(history.loginAt).isNotNull()
    }

    @Test
    @DisplayName("IP 주소와 User-Agent 없이 로그인 이력을 생성할 수 있다")
    fun createWithoutOptionalFields() {
        // when
        val history = MemberLoginHistory.create(
            memberId = 1L,
            provider = OAuthProvider.KAKAO,
        )

        // then
        assertThat(history.memberId).isEqualTo(1L)
        assertThat(history.provider).isEqualTo(OAuthProvider.KAKAO)
        assertThat(history.ipAddress).isNull()
        assertThat(history.userAgent).isNull()
    }
}

package com.nbang.nbangapi.domain.exclusion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ExclusionTest {

    @Test
    @DisplayName("제외 정보를 생성할 수 있다")
    fun create() {
        // given
        val reason = "술 안마심"
        val participantId = 1L
        val roundId = 1L

        // when
        val exclusion = Exclusion(
            reason = reason,
            participantId = participantId,
            roundId = roundId
        )

        // then
        assertThat(exclusion.reason).isEqualTo(reason)
        assertThat(exclusion.participantId).isEqualTo(participantId)
        assertThat(exclusion.roundId).isEqualTo(roundId)
    }
}

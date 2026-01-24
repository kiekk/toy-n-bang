package com.pay.dutchpayapi.domain.participant

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ParticipantTest {

    @Test
    @DisplayName("참여자를 생성할 수 있다")
    fun create() {
        // given
        val name = "홍길동"
        val gatheringId = 1L

        // when
        val participant = Participant(
            name = name,
            gatheringId = gatheringId
        )

        // then
        assertThat(participant.name).isEqualTo(name)
        assertThat(participant.gatheringId).isEqualTo(gatheringId)
    }

    @Test
    @DisplayName("참여자 이름을 수정할 수 있다")
    fun updateName() {
        // given
        val participant = Participant(
            name = "원래 이름",
            gatheringId = 1L
        )

        val newName = "수정된 이름"

        // when
        participant.updateName(newName)

        // then
        assertThat(participant.name).isEqualTo(newName)
    }
}

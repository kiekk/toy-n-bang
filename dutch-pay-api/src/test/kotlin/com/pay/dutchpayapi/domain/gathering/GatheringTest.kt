package com.pay.dutchpayapi.domain.gathering

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GatheringTest {

    @Test
    @DisplayName("모임을 생성할 수 있다")
    fun create() {
        // given
        val name = "제주도 여행"
        val startDate = LocalDate.of(2025, 1, 15)
        val endDate = LocalDate.of(2025, 1, 17)

        // when
        val gathering = Gathering(
            name = name,
            startDate = startDate,
            endDate = endDate
        )

        // then
        assertThat(gathering.name).isEqualTo(name)
        assertThat(gathering.startDate).isEqualTo(startDate)
        assertThat(gathering.endDate).isEqualTo(endDate)
    }

    @Test
    @DisplayName("모임 정보를 수정할 수 있다")
    fun update() {
        // given
        val gathering = Gathering(
            name = "원래 이름",
            startDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2025, 1, 3)
        )

        val newName = "수정된 이름"
        val newStartDate = LocalDate.of(2025, 2, 1)
        val newEndDate = LocalDate.of(2025, 2, 5)

        // when
        gathering.update(newName, newStartDate, newEndDate)

        // then
        assertThat(gathering.name).isEqualTo(newName)
        assertThat(gathering.startDate).isEqualTo(newStartDate)
        assertThat(gathering.endDate).isEqualTo(newEndDate)
    }
}

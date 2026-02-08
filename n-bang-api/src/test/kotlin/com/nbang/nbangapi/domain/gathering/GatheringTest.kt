package com.nbang.nbangapi.domain.gathering

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
        val type = GatheringType.TRAVEL
        val startDate = LocalDate.of(2025, 1, 15)
        val endDate = LocalDate.of(2025, 1, 17)

        // when
        val gathering = Gathering(
            memberId = 1L,
            name = name,
            type = type,
            startDate = startDate,
            endDate = endDate
        )

        // then
        assertThat(gathering.name).isEqualTo(name)
        assertThat(gathering.type).isEqualTo(type)
        assertThat(gathering.startDate).isEqualTo(startDate)
        assertThat(gathering.endDate).isEqualTo(endDate)
    }

    @Test
    @DisplayName("모임 정보를 수정할 수 있다")
    fun update() {
        // given
        val gathering = Gathering(
            memberId = 1L,
            name = "원래 이름",
            type = GatheringType.TRAVEL,
            startDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2025, 1, 3)
        )

        val newName = "수정된 이름"
        val newType = GatheringType.DINING
        val newStartDate = LocalDate.of(2025, 2, 1)
        val newEndDate = LocalDate.of(2025, 2, 5)

        // when
        gathering.update(newName, newType, newStartDate, newEndDate)

        // then
        assertThat(gathering.name).isEqualTo(newName)
        assertThat(gathering.type).isEqualTo(newType)
        assertThat(gathering.startDate).isEqualTo(newStartDate)
        assertThat(gathering.endDate).isEqualTo(newEndDate)
    }

    @Test
    @DisplayName("모임 소유자인지 확인할 수 있다")
    fun isOwnedBy() {
        // given
        val memberId = 1L
        val gathering = Gathering(
            memberId = memberId,
            name = "모임",
            type = GatheringType.MEETING,
            startDate = LocalDate.now(),
            endDate = LocalDate.now()
        )

        // when & then
        assertThat(gathering.isOwnedBy(memberId)).isTrue()
        assertThat(gathering.isOwnedBy(999L)).isFalse()
    }

    @Test
    @DisplayName("모임 타입 기본값은 OTHER이다")
    fun defaultType() {
        // given & when
        val gathering = Gathering(
            memberId = 1L,
            name = "모임",
            startDate = LocalDate.now(),
            endDate = LocalDate.now()
        )

        // then
        assertThat(gathering.type).isEqualTo(GatheringType.OTHER)
    }
}

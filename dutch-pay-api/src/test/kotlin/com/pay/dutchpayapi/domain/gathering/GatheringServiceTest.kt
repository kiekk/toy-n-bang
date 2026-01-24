package com.pay.dutchpayapi.domain.gathering

import com.pay.dutchpayapi.support.IntegrationTest
import com.pay.dutchpayapi.support.error.CoreException
import com.pay.dutchpayapi.support.error.ErrorType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class GatheringServiceTest @Autowired constructor(
    private val gatheringService: GatheringService
) : IntegrationTest() {

    @Test
    @DisplayName("모임을 생성할 수 있다")
    fun create() {
        // given
        val name = "제주도 여행"
        val startDate = LocalDate.of(2025, 1, 15)
        val endDate = LocalDate.of(2025, 1, 17)

        // when
        val gathering = gatheringService.create(name, startDate, endDate)

        // then
        assertThat(gathering.id).isNotNull()
        assertThat(gathering.name).isEqualTo(name)
        assertThat(gathering.startDate).isEqualTo(startDate)
        assertThat(gathering.endDate).isEqualTo(endDate)
    }

    @Test
    @DisplayName("ID로 모임을 조회할 수 있다")
    fun findById() {
        // given
        val created = gatheringService.create(
            name = "제주도 여행",
            startDate = LocalDate.of(2025, 1, 15),
            endDate = LocalDate.of(2025, 1, 17)
        )

        // when
        val found = gatheringService.findById(created.id!!)

        // then
        assertThat(found.id).isEqualTo(created.id)
        assertThat(found.name).isEqualTo(created.name)
    }

    @Test
    @DisplayName("존재하지 않는 모임 조회 시 예외가 발생한다")
    fun findByIdNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { gatheringService.findById(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.GATHERING_NOT_FOUND)
    }

    @Test
    @DisplayName("모든 모임 목록을 조회할 수 있다")
    fun findAll() {
        // given
        gatheringService.create("여행1", LocalDate.now(), LocalDate.now().plusDays(1))
        gatheringService.create("여행2", LocalDate.now(), LocalDate.now().plusDays(2))

        // when
        val gatherings = gatheringService.findAll()

        // then
        assertThat(gatherings).hasSizeGreaterThanOrEqualTo(2)
    }

    @Test
    @DisplayName("모임 정보를 수정할 수 있다")
    fun update() {
        // given
        val created = gatheringService.create(
            name = "원래 이름",
            startDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2025, 1, 3)
        )

        val newName = "수정된 이름"
        val newStartDate = LocalDate.of(2025, 2, 1)
        val newEndDate = LocalDate.of(2025, 2, 5)

        // when
        val updated = gatheringService.update(created.id!!, newName, newStartDate, newEndDate)

        // then
        assertThat(updated.name).isEqualTo(newName)
        assertThat(updated.startDate).isEqualTo(newStartDate)
        assertThat(updated.endDate).isEqualTo(newEndDate)
    }

    @Test
    @DisplayName("존재하지 않는 모임 수정 시 예외가 발생한다")
    fun updateNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy {
            gatheringService.update(nonExistentId, "이름", LocalDate.now(), LocalDate.now())
        }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.GATHERING_NOT_FOUND)
    }

    @Test
    @DisplayName("모임을 삭제할 수 있다")
    fun delete() {
        // given
        val created = gatheringService.create(
            name = "삭제할 모임",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )

        // when
        gatheringService.delete(created.id!!)

        // then
        assertThatThrownBy { gatheringService.findById(created.id!!) }
            .isInstanceOf(CoreException::class.java)
    }

    @Test
    @DisplayName("존재하지 않는 모임 삭제 시 예외가 발생한다")
    fun deleteNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { gatheringService.delete(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.GATHERING_NOT_FOUND)
    }

    @Test
    @DisplayName("모임 존재 여부를 확인할 수 있다")
    fun existsById() {
        // given
        val created = gatheringService.create(
            name = "모임",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )

        // when & then
        assertThat(gatheringService.existsById(created.id!!)).isTrue()
        assertThat(gatheringService.existsById(999999L)).isFalse()
    }
}

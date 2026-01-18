package com.pay.dutchpayapi.application.gathering

import com.pay.dutchpayapi.support.TestcontainersConfiguration
import com.pay.dutchpayapi.support.error.BusinessException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration::class)
class GatheringServiceTest @Autowired constructor(
    private val gatheringService: GatheringService
) {

    @Test
    @DisplayName("모임을 생성할 수 있다")
    fun create() {
        // given
        val request = GatheringCreateRequest(
            name = "제주도 여행",
            startDate = LocalDate.of(2025, 1, 15),
            endDate = LocalDate.of(2025, 1, 17),
            participantNames = listOf("홍길동", "김철수", "이영희")
        )

        // when
        val response = gatheringService.create(request)

        // then
        assertThat(response.id).isNotNull()
        assertThat(response.name).isEqualTo("제주도 여행")
        assertThat(response.startDate).isEqualTo(LocalDate.of(2025, 1, 15))
        assertThat(response.endDate).isEqualTo(LocalDate.of(2025, 1, 17))
        assertThat(response.participants).hasSize(3)
    }

    @Test
    @DisplayName("모임을 조회할 수 있다")
    fun findById() {
        // given
        val request = GatheringCreateRequest(
            name = "제주도 여행",
            startDate = LocalDate.of(2025, 1, 15),
            endDate = LocalDate.of(2025, 1, 17),
            participantNames = listOf("홍길동", "김철수")
        )
        val created = gatheringService.create(request)

        // when
        val response = gatheringService.findById(created.id)

        // then
        assertThat(response.id).isEqualTo(created.id)
        assertThat(response.name).isEqualTo("제주도 여행")
        assertThat(response.participants).hasSize(2)
    }

    @Test
    @DisplayName("모임 목록을 조회할 수 있다")
    fun findAll() {
        // given
        gatheringService.create(
            GatheringCreateRequest(
                name = "여행1",
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                participantNames = listOf("A", "B")
            )
        )
        gatheringService.create(
            GatheringCreateRequest(
                name = "여행2",
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(2),
                participantNames = listOf("C", "D")
            )
        )

        // when
        val responses = gatheringService.findAll()

        // then
        assertThat(responses).hasSizeGreaterThanOrEqualTo(2)
    }

    @Test
    @DisplayName("모임을 수정할 수 있다")
    fun update() {
        // given
        val created = gatheringService.create(
            GatheringCreateRequest(
                name = "원래 이름",
                startDate = LocalDate.of(2025, 1, 1),
                endDate = LocalDate.of(2025, 1, 3),
                participantNames = listOf("A")
            )
        )

        val updateRequest = GatheringUpdateRequest(
            name = "수정된 이름",
            startDate = LocalDate.of(2025, 2, 1),
            endDate = LocalDate.of(2025, 2, 5)
        )

        // when
        val updated = gatheringService.update(created.id, updateRequest)

        // then
        assertThat(updated.name).isEqualTo("수정된 이름")
        assertThat(updated.startDate).isEqualTo(LocalDate.of(2025, 2, 1))
        assertThat(updated.endDate).isEqualTo(LocalDate.of(2025, 2, 5))
    }

    @Test
    @DisplayName("모임을 삭제할 수 있다")
    fun delete() {
        // given
        val created = gatheringService.create(
            GatheringCreateRequest(
                name = "삭제할 모임",
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                participantNames = listOf("A")
            )
        )

        // when
        gatheringService.delete(created.id)

        // then
        assertThatThrownBy { gatheringService.findById(created.id) }
            .isInstanceOf(BusinessException::class.java)
    }

    @Test
    @DisplayName("존재하지 않는 모임 조회 시 예외가 발생한다")
    fun findByIdNotFound() {
        // given
        val randomId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { gatheringService.findById(randomId) }
            .isInstanceOf(BusinessException::class.java)
    }
}

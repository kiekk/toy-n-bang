package com.nbang.nbangapi.application.gathering

import com.nbang.nbangapi.application.gathering.request.GatheringCreateRequest
import com.nbang.nbangapi.application.gathering.request.GatheringUpdateRequest
import com.nbang.nbangapi.domain.gathering.GatheringType
import com.nbang.nbangapi.support.IntegrationTest
import com.nbang.nbangapi.support.error.CoreException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class GatheringFacadeTest @Autowired constructor(
    private val gatheringFacade: GatheringFacade
) : IntegrationTest() {

    private val testMemberId = 1L

    @Test
    @DisplayName("모임을 생성할 수 있다")
    fun create() {
        // given
        val request = GatheringCreateRequest(
            name = "제주도 여행",
            type = GatheringType.TRAVEL,
            startDate = LocalDate.of(2025, 1, 15),
            endDate = LocalDate.of(2025, 1, 17),
            participantNames = listOf("홍길동", "김철수", "이영희")
        )

        // when
        val response = gatheringFacade.create(testMemberId, request)

        // then
        assertThat(response.id).isNotNull()
        assertThat(response.name).isEqualTo("제주도 여행")
        assertThat(response.type).isEqualTo(GatheringType.TRAVEL)
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
            type = GatheringType.TRAVEL,
            startDate = LocalDate.of(2025, 1, 15),
            endDate = LocalDate.of(2025, 1, 17),
            participantNames = listOf("홍길동", "김철수")
        )
        val created = gatheringFacade.create(testMemberId, request)

        // when
        val response = gatheringFacade.findById(created.id, testMemberId)

        // then
        assertThat(response.id).isEqualTo(created.id)
        assertThat(response.name).isEqualTo("제주도 여행")
        assertThat(response.type).isEqualTo(GatheringType.TRAVEL)
        assertThat(response.participants).hasSize(2)
    }

    @Test
    @DisplayName("회원별 모임 목록을 조회할 수 있다")
    fun findAllByMemberId() {
        // given
        gatheringFacade.create(
            testMemberId,
            GatheringCreateRequest(
                name = "여행1",
                type = GatheringType.TRAVEL,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                participantNames = listOf("A", "B")
            )
        )
        gatheringFacade.create(
            testMemberId,
            GatheringCreateRequest(
                name = "회식",
                type = GatheringType.DINING,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(2),
                participantNames = listOf("C", "D")
            )
        )

        // when
        val responses = gatheringFacade.findAllByMemberId(testMemberId)

        // then
        assertThat(responses).hasSizeGreaterThanOrEqualTo(2)
    }

    @Test
    @DisplayName("모임을 수정할 수 있다")
    fun update() {
        // given
        val created = gatheringFacade.create(
            testMemberId,
            GatheringCreateRequest(
                name = "원래 이름",
                type = GatheringType.TRAVEL,
                startDate = LocalDate.of(2025, 1, 1),
                endDate = LocalDate.of(2025, 1, 3),
                participantNames = listOf("A")
            )
        )

        val updateRequest = GatheringUpdateRequest(
            name = "수정된 이름",
            type = GatheringType.DINING,
            startDate = LocalDate.of(2025, 2, 1),
            endDate = LocalDate.of(2025, 2, 5)
        )

        // when
        val updated = gatheringFacade.update(created.id, testMemberId, updateRequest)

        // then
        assertThat(updated.name).isEqualTo("수정된 이름")
        assertThat(updated.type).isEqualTo(GatheringType.DINING)
        assertThat(updated.startDate).isEqualTo(LocalDate.of(2025, 2, 1))
        assertThat(updated.endDate).isEqualTo(LocalDate.of(2025, 2, 5))
    }

    @Test
    @DisplayName("모임을 삭제할 수 있다")
    fun delete() {
        // given
        val created = gatheringFacade.create(
            testMemberId,
            GatheringCreateRequest(
                name = "삭제할 모임",
                type = GatheringType.MEETING,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                participantNames = listOf("A")
            )
        )

        // when
        gatheringFacade.delete(created.id, testMemberId)

        // then
        assertThatThrownBy { gatheringFacade.findById(created.id, testMemberId) }
            .isInstanceOf(CoreException::class.java)
    }

    @Test
    @DisplayName("존재하지 않는 모임 조회 시 예외가 발생한다")
    fun findByIdNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { gatheringFacade.findById(nonExistentId, testMemberId) }
            .isInstanceOf(CoreException::class.java)
    }

    @Test
    @DisplayName("모임 타입 기본값은 OTHER이다")
    fun createWithDefaultType() {
        // given
        val request = GatheringCreateRequest(
            name = "기본 타입 모임",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            participantNames = listOf("A")
        )

        // when
        val response = gatheringFacade.create(testMemberId, request)

        // then
        assertThat(response.type).isEqualTo(GatheringType.OTHER)
    }
}

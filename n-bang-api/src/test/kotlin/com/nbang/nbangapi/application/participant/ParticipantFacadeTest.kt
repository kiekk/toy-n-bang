package com.nbang.nbangapi.application.participant

import com.nbang.nbangapi.application.gathering.GatheringFacade
import com.nbang.nbangapi.application.gathering.request.GatheringCreateRequest
import com.nbang.nbangapi.application.participant.request.ParticipantCreateRequest
import com.nbang.nbangapi.support.IntegrationTest
import com.nbang.nbangapi.support.error.CoreException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class ParticipantFacadeTest @Autowired constructor(
    private val participantFacade: ParticipantFacade,
    private val gatheringFacade: GatheringFacade
) : IntegrationTest() {

    private val testMemberId = 1L
    private var gatheringId: Long = 0L

    @BeforeEach
    fun setUp() {
        val gathering = gatheringFacade.create(
            testMemberId,
            GatheringCreateRequest(
                name = "테스트 모임",
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                participantNames = null
            )
        )
        gatheringId = gathering.id
    }

    @Test
    @DisplayName("모임에 참여자를 추가할 수 있다")
    fun addToGathering() {
        // given
        val request = ParticipantCreateRequest(name = "홍길동")

        // when
        val response = participantFacade.addToGathering(gatheringId, request)

        // then
        assertThat(response.id).isNotNull()
        assertThat(response.name).isEqualTo("홍길동")
    }

    @Test
    @DisplayName("모임의 참여자 목록을 조회할 수 있다")
    fun findByGatheringId() {
        // given
        participantFacade.addToGathering(gatheringId, ParticipantCreateRequest(name = "홍길동"))
        participantFacade.addToGathering(gatheringId, ParticipantCreateRequest(name = "김철수"))
        participantFacade.addToGathering(gatheringId, ParticipantCreateRequest(name = "이영희"))

        // when
        val participants = participantFacade.findByGatheringId(gatheringId)

        // then
        assertThat(participants).hasSize(3)
        assertThat(participants.map { it.name }).containsExactlyInAnyOrder("홍길동", "김철수", "이영희")
    }

    @Test
    @DisplayName("참여자를 삭제할 수 있다")
    fun delete() {
        // given
        val participant = participantFacade.addToGathering(gatheringId, ParticipantCreateRequest(name = "홍길동"))

        // when
        participantFacade.delete(participant.id)

        // then
        val participants = participantFacade.findByGatheringId(gatheringId)
        assertThat(participants).isEmpty()
    }

    @Test
    @DisplayName("존재하지 않는 모임에 참여자 추가 시 예외가 발생한다")
    fun addToNonExistentGathering() {
        // given
        val nonExistentGatheringId = 999999L
        val request = ParticipantCreateRequest(name = "홍길동")

        // when & then
        assertThatThrownBy { participantFacade.addToGathering(nonExistentGatheringId, request) }
            .isInstanceOf(CoreException::class.java)
    }

    @Test
    @DisplayName("존재하지 않는 참여자 삭제 시 예외가 발생한다")
    fun deleteNonExistent() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { participantFacade.delete(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
    }
}

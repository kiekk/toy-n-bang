package com.nbang.nbangapi.domain.participant

import com.nbang.nbangapi.domain.gathering.GatheringService
import com.nbang.nbangapi.support.IntegrationTest
import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class ParticipantServiceTest @Autowired constructor(
    private val participantService: ParticipantService,
    private val gatheringService: GatheringService
) : IntegrationTest() {

    private var gatheringId: Long = 0L

    @BeforeEach
    fun setUp() {
        val gathering = gatheringService.create(
            name = "테스트 모임",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )
        gatheringId = gathering.id!!
    }

    @Test
    @DisplayName("참여자를 생성할 수 있다")
    fun create() {
        // given
        val name = "홍길동"

        // when
        val participant = participantService.create(name, gatheringId)

        // then
        assertThat(participant.id).isNotNull()
        assertThat(participant.name).isEqualTo(name)
        assertThat(participant.gatheringId).isEqualTo(gatheringId)
    }

    @Test
    @DisplayName("ID로 참여자를 조회할 수 있다")
    fun findById() {
        // given
        val created = participantService.create("홍길동", gatheringId)

        // when
        val found = participantService.findById(created.id!!)

        // then
        assertThat(found.id).isEqualTo(created.id)
        assertThat(found.name).isEqualTo(created.name)
    }

    @Test
    @DisplayName("존재하지 않는 참여자 조회 시 예외가 발생한다")
    fun findByIdNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { participantService.findById(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.PARTICIPANT_NOT_FOUND)
    }

    @Test
    @DisplayName("모임별 참여자 목록을 조회할 수 있다")
    fun findByGatheringId() {
        // given
        participantService.create("홍길동", gatheringId)
        participantService.create("김철수", gatheringId)
        participantService.create("이영희", gatheringId)

        // when
        val participants = participantService.findByGatheringId(gatheringId)

        // then
        assertThat(participants).hasSize(3)
        assertThat(participants.map { it.name }).containsExactlyInAnyOrder("홍길동", "김철수", "이영희")
    }

    @Test
    @DisplayName("참여자를 삭제할 수 있다")
    fun delete() {
        // given
        val created = participantService.create("홍길동", gatheringId)

        // when
        participantService.delete(created.id!!)

        // then
        assertThatThrownBy { participantService.findById(created.id!!) }
            .isInstanceOf(CoreException::class.java)
    }

    @Test
    @DisplayName("존재하지 않는 참여자 삭제 시 예외가 발생한다")
    fun deleteNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { participantService.delete(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.PARTICIPANT_NOT_FOUND)
    }

    @Test
    @DisplayName("참여자 존재 여부를 확인할 수 있다")
    fun existsById() {
        // given
        val created = participantService.create("홍길동", gatheringId)

        // when & then
        assertThat(participantService.existsById(created.id!!)).isTrue()
        assertThat(participantService.existsById(999999L)).isFalse()
    }
}

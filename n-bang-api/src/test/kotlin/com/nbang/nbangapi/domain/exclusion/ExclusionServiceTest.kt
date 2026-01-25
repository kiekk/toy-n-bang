package com.nbang.nbangapi.domain.exclusion

import com.nbang.nbangapi.domain.gathering.GatheringService
import com.nbang.nbangapi.domain.participant.ParticipantService
import com.nbang.nbangapi.domain.round.SettlementRoundService
import com.nbang.nbangapi.support.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate

class ExclusionServiceTest @Autowired constructor(
    private val exclusionService: ExclusionService,
    private val roundService: SettlementRoundService,
    private val participantService: ParticipantService,
    private val gatheringService: GatheringService
) : IntegrationTest() {

    private var roundId: Long = 0L
    private var participantId: Long = 0L

    @BeforeEach
    fun setUp() {
        val gathering = gatheringService.create(
            name = "테스트 모임",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )

        val participant = participantService.create("홍길동", gathering.id!!)
        participantId = participant.id!!

        val round = roundService.create("1차", BigDecimal("30000"), participantId, gathering.id!!)
        roundId = round.id!!
    }

    @Test
    @DisplayName("제외 정보를 생성할 수 있다")
    fun create() {
        // given
        val reason = "술 안마심"

        // when
        val exclusion = exclusionService.create(reason, participantId, roundId)

        // then
        assertThat(exclusion.id).isNotNull()
        assertThat(exclusion.reason).isEqualTo(reason)
        assertThat(exclusion.participantId).isEqualTo(participantId)
        assertThat(exclusion.roundId).isEqualTo(roundId)
    }

    @Test
    @DisplayName("라운드별 제외 목록을 조회할 수 있다")
    fun findByRoundId() {
        // given
        val participant2 = participantService.create("김철수", gatheringService.findAll().first().id!!)
        exclusionService.create("술 안마심", participantId, roundId)
        exclusionService.create("먼저 귀가", participant2.id!!, roundId)

        // when
        val exclusions = exclusionService.findByRoundId(roundId)

        // then
        assertThat(exclusions).hasSize(2)
        assertThat(exclusions.map { it.reason }).containsExactlyInAnyOrder("술 안마심", "먼저 귀가")
    }

    @Test
    @DisplayName("라운드별 제외 정보를 삭제할 수 있다")
    fun deleteByRoundId() {
        // given
        exclusionService.create("술 안마심", participantId, roundId)
        exclusionService.create("먼저 귀가", participantId, roundId)

        // when
        exclusionService.deleteByRoundId(roundId)

        // then
        val exclusions = exclusionService.findByRoundId(roundId)
        assertThat(exclusions).isEmpty()
    }
}

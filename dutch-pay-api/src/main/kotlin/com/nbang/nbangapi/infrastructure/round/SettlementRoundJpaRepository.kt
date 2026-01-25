package com.nbang.nbangapi.infrastructure.round

import com.nbang.nbangapi.domain.round.SettlementRound
import org.springframework.data.jpa.repository.JpaRepository

interface SettlementRoundJpaRepository : JpaRepository<SettlementRound, Long> {

    fun findByGatheringId(gatheringId: Long): List<SettlementRound>
}

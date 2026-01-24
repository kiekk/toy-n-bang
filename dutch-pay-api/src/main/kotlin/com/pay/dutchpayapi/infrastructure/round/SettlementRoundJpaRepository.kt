package com.pay.dutchpayapi.infrastructure.round

import com.pay.dutchpayapi.domain.round.SettlementRound
import org.springframework.data.jpa.repository.JpaRepository

interface SettlementRoundJpaRepository : JpaRepository<SettlementRound, Long> {

    fun findByGatheringId(gatheringId: Long): List<SettlementRound>
}

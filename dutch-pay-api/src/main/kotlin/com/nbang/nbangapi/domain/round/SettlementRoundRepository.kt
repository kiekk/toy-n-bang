package com.nbang.nbangapi.domain.round

import java.util.*

interface SettlementRoundRepository {
    fun save(round: SettlementRound): SettlementRound
    fun findById(id: Long): Optional<SettlementRound>
    fun findByGatheringId(gatheringId: Long): List<SettlementRound>
    fun existsById(id: Long): Boolean
    fun deleteById(id: Long)
}

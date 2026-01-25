package com.nbang.nbangapi.infrastructure.round

import com.nbang.nbangapi.domain.round.SettlementRound
import com.nbang.nbangapi.domain.round.SettlementRoundRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class SettlementRoundRepositoryImpl(
    private val settlementRoundJpaRepository: SettlementRoundJpaRepository
) : SettlementRoundRepository {

    override fun save(round: SettlementRound): SettlementRound {
        return settlementRoundJpaRepository.save(round)
    }

    override fun findById(id: Long): Optional<SettlementRound> {
        return settlementRoundJpaRepository.findById(id)
    }

    override fun findByGatheringId(gatheringId: Long): List<SettlementRound> {
        return settlementRoundJpaRepository.findByGatheringId(gatheringId)
    }

    override fun existsById(id: Long): Boolean {
        return settlementRoundJpaRepository.existsById(id)
    }

    override fun deleteById(id: Long) {
        settlementRoundJpaRepository.deleteById(id)
    }
}

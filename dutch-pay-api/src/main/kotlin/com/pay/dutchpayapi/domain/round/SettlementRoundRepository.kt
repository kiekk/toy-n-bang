package com.pay.dutchpayapi.domain.round

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface SettlementRoundRepository : JpaRepository<SettlementRound, UUID> {

    fun findByGatheringId(gatheringId: UUID): List<SettlementRound>

    @Query("""
        SELECT r FROM SettlementRound r
        LEFT JOIN FETCH r.payer
        LEFT JOIN FETCH r._exclusions e
        LEFT JOIN FETCH e.participant
        WHERE r.id = :id
    """)
    fun findByIdWithDetails(id: UUID): SettlementRound?

    @Query("""
        SELECT r FROM SettlementRound r
        LEFT JOIN FETCH r.payer
        LEFT JOIN FETCH r._exclusions e
        LEFT JOIN FETCH e.participant
        WHERE r.gathering.id = :gatheringId
    """)
    fun findByGatheringIdWithDetails(gatheringId: UUID): List<SettlementRound>
}

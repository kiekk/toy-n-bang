package com.pay.dutchpayapi.domain.gathering

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface GatheringRepository : JpaRepository<Gathering, UUID> {

    @Query("""
        SELECT g FROM Gathering g
        LEFT JOIN FETCH g._participants
        LEFT JOIN FETCH g._rounds r
        LEFT JOIN FETCH r.payer
        LEFT JOIN FETCH r._exclusions e
        LEFT JOIN FETCH e.participant
        WHERE g.id = :id
    """)
    fun findByIdWithDetails(id: UUID): Gathering?
}

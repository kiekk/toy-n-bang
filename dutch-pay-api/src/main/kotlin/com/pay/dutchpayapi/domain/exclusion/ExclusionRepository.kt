package com.pay.dutchpayapi.domain.exclusion

interface ExclusionRepository {
    fun save(exclusion: Exclusion): Exclusion
    fun findByRoundId(roundId: Long): List<Exclusion>
    fun deleteByRoundId(roundId: Long)
}

package com.pay.dutchpayapi.domain.exclusion

import java.util.*

interface ExclusionRepository {
    fun save(exclusion: Exclusion): Exclusion
    fun saveAll(exclusions: List<Exclusion>): List<Exclusion>
    fun findById(id: Long): Optional<Exclusion>
    fun findByRoundId(roundId: Long): List<Exclusion>
    fun deleteByRoundId(roundId: Long)
    fun deleteById(id: Long)
}

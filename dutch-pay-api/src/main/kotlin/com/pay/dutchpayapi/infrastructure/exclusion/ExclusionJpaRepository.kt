package com.pay.dutchpayapi.infrastructure.exclusion

import com.pay.dutchpayapi.domain.exclusion.Exclusion
import org.springframework.data.jpa.repository.JpaRepository

interface ExclusionJpaRepository : JpaRepository<Exclusion, Long> {

    fun findByRoundId(roundId: Long): List<Exclusion>

    fun deleteByRoundId(roundId: Long)
}

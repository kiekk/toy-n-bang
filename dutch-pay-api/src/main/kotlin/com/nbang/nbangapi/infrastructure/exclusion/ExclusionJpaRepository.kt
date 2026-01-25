package com.nbang.nbangapi.infrastructure.exclusion

import com.nbang.nbangapi.domain.exclusion.Exclusion
import org.springframework.data.jpa.repository.JpaRepository

interface ExclusionJpaRepository : JpaRepository<Exclusion, Long> {

    fun findByRoundId(roundId: Long): List<Exclusion>

    fun deleteByRoundId(roundId: Long)
}

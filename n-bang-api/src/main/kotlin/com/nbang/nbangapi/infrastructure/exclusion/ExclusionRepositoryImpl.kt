package com.nbang.nbangapi.infrastructure.exclusion

import com.nbang.nbangapi.domain.exclusion.Exclusion
import com.nbang.nbangapi.domain.exclusion.ExclusionRepository
import org.springframework.stereotype.Repository

@Repository
class ExclusionRepositoryImpl(
    private val exclusionJpaRepository: ExclusionJpaRepository
) : ExclusionRepository {

    override fun save(exclusion: Exclusion): Exclusion {
        return exclusionJpaRepository.save(exclusion)
    }

    override fun findByRoundId(roundId: Long): List<Exclusion> {
        return exclusionJpaRepository.findByRoundId(roundId)
    }

    override fun deleteByRoundId(roundId: Long) {
        exclusionJpaRepository.deleteByRoundId(roundId)
    }
}

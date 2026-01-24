package com.pay.dutchpayapi.infrastructure.exclusion

import com.pay.dutchpayapi.domain.exclusion.Exclusion
import com.pay.dutchpayapi.domain.exclusion.ExclusionRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ExclusionRepositoryImpl(
    private val exclusionJpaRepository: ExclusionJpaRepository
) : ExclusionRepository {

    override fun save(exclusion: Exclusion): Exclusion {
        return exclusionJpaRepository.save(exclusion)
    }

    override fun saveAll(exclusions: List<Exclusion>): List<Exclusion> {
        return exclusionJpaRepository.saveAll(exclusions)
    }

    override fun findById(id: Long): Optional<Exclusion> {
        return exclusionJpaRepository.findById(id)
    }

    override fun findByRoundId(roundId: Long): List<Exclusion> {
        return exclusionJpaRepository.findByRoundId(roundId)
    }

    override fun deleteByRoundId(roundId: Long) {
        exclusionJpaRepository.deleteByRoundId(roundId)
    }

    override fun deleteById(id: Long) {
        exclusionJpaRepository.deleteById(id)
    }
}

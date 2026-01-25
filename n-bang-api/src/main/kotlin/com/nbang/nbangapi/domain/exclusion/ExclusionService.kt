package com.nbang.nbangapi.domain.exclusion

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ExclusionService(
    private val exclusionRepository: ExclusionRepository
) {

    @Transactional
    fun create(reason: String, participantId: Long, roundId: Long): Exclusion {
        return exclusionRepository.save(
            Exclusion(
                reason = reason,
                participantId = participantId,
                roundId = roundId
            )
        )
    }

    fun findByRoundId(roundId: Long): List<Exclusion> {
        return exclusionRepository.findByRoundId(roundId)
    }

    @Transactional
    fun deleteByRoundId(roundId: Long) {
        exclusionRepository.deleteByRoundId(roundId)
    }
}

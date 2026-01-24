package com.pay.dutchpayapi.domain.exclusion

import org.springframework.stereotype.Service

@Service
class ExclusionService(
    private val exclusionRepository: ExclusionRepository
) {

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

    fun deleteByRoundId(roundId: Long) {
        exclusionRepository.deleteByRoundId(roundId)
    }
}

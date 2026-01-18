package com.pay.dutchpayapi.application.round

import com.pay.dutchpayapi.domain.exclusion.Exclusion
import com.pay.dutchpayapi.domain.gathering.GatheringRepository
import com.pay.dutchpayapi.domain.participant.ParticipantRepository
import com.pay.dutchpayapi.domain.round.SettlementRound
import com.pay.dutchpayapi.domain.round.SettlementRoundRepository
import com.pay.dutchpayapi.support.error.BusinessException
import com.pay.dutchpayapi.support.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class SettlementRoundService(
    private val roundRepository: SettlementRoundRepository,
    private val gatheringRepository: GatheringRepository,
    private val participantRepository: ParticipantRepository
) {

    @Transactional
    fun create(gatheringId: UUID, request: RoundCreateRequest): RoundResponse {
        val gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow { BusinessException(ErrorCode.GATHERING_NOT_FOUND) }

        val payer = participantRepository.findById(request.payerId)
            .orElseThrow { BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND) }

        val round = SettlementRound(
            title = request.title,
            amount = request.amount,
            payer = payer
        )

        gathering.addRound(round)

        request.exclusions?.forEach { exclusionRequest ->
            addExclusion(round, exclusionRequest)
        }

        val saved = roundRepository.save(round)
        return RoundResponse.from(saved)
    }

    fun findByGatheringId(gatheringId: UUID): List<RoundResponse> {
        if (!gatheringRepository.existsById(gatheringId)) {
            throw BusinessException(ErrorCode.GATHERING_NOT_FOUND)
        }

        return roundRepository.findByGatheringIdWithDetails(gatheringId)
            .map { RoundResponse.from(it) }
    }

    fun findById(id: UUID): RoundResponse {
        val round = roundRepository.findByIdWithDetails(id)
            ?: throw BusinessException(ErrorCode.ROUND_NOT_FOUND)
        return RoundResponse.from(round)
    }

    @Transactional
    fun update(id: UUID, request: RoundUpdateRequest): RoundResponse {
        val round = roundRepository.findByIdWithDetails(id)
            ?: throw BusinessException(ErrorCode.ROUND_NOT_FOUND)

        val payer = participantRepository.findById(request.payerId)
            .orElseThrow { BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND) }

        round.update(request.title, request.amount, payer)
        round.clearExclusions()

        request.exclusions?.forEach { exclusionRequest ->
            addExclusion(round, exclusionRequest)
        }

        return RoundResponse.from(round)
    }

    @Transactional
    fun delete(id: UUID) {
        if (!roundRepository.existsById(id)) {
            throw BusinessException(ErrorCode.ROUND_NOT_FOUND)
        }
        roundRepository.deleteById(id)
    }

    @Transactional
    fun updateReceiptImage(id: UUID, imageUrl: String): RoundResponse {
        val round = roundRepository.findByIdWithDetails(id)
            ?: throw BusinessException(ErrorCode.ROUND_NOT_FOUND)

        round.updateReceiptImageUrl(imageUrl)
        return RoundResponse.from(round)
    }

    private fun addExclusion(round: SettlementRound, exclusionRequest: ExclusionRequest) {
        val participant = participantRepository.findById(exclusionRequest.participantId)
            .orElseThrow { BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND) }

        val exclusion = Exclusion(
            reason = exclusionRequest.reason,
            participant = participant
        )
        round.addExclusion(exclusion)
    }
}

package com.pay.dutchpayapi.application.gathering

import com.pay.dutchpayapi.domain.gathering.Gathering
import com.pay.dutchpayapi.domain.gathering.GatheringRepository
import com.pay.dutchpayapi.domain.participant.Participant
import com.pay.dutchpayapi.support.error.BusinessException
import com.pay.dutchpayapi.support.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class GatheringService(
    private val gatheringRepository: GatheringRepository
) {

    @Transactional
    fun create(request: GatheringCreateRequest): GatheringResponse {
        val gathering = Gathering(
            name = request.name,
            startDate = request.startDate,
            endDate = request.endDate
        )

        request.participantNames?.forEach { name ->
            gathering.addParticipant(Participant(name = name))
        }

        val saved = gatheringRepository.save(gathering)
        return GatheringResponse.from(saved)
    }

    fun findAll(): List<GatheringResponse> {
        return gatheringRepository.findAll()
            .map { GatheringResponse.simpleFrom(it) }
    }

    fun findById(id: UUID): GatheringResponse {
        val gathering = gatheringRepository.findByIdWithDetails(id)
            ?: throw BusinessException(ErrorCode.GATHERING_NOT_FOUND)
        return GatheringResponse.from(gathering)
    }

    @Transactional
    fun update(id: UUID, request: GatheringUpdateRequest): GatheringResponse {
        val gathering = gatheringRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.GATHERING_NOT_FOUND) }

        gathering.update(request.name, request.startDate, request.endDate)
        return GatheringResponse.simpleFrom(gathering)
    }

    @Transactional
    fun delete(id: UUID) {
        if (!gatheringRepository.existsById(id)) {
            throw BusinessException(ErrorCode.GATHERING_NOT_FOUND)
        }
        gatheringRepository.deleteById(id)
    }
}

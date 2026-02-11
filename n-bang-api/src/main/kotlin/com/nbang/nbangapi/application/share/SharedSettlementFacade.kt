package com.nbang.nbangapi.application.share

import com.nbang.nbangapi.application.calculation.CalculationFacade
import com.nbang.nbangapi.application.round.SettlementRoundFacade
import com.nbang.nbangapi.application.share.response.SharedSettlementLinkResponse
import com.nbang.nbangapi.application.share.response.SharedSettlementResponse
import com.nbang.nbangapi.domain.gathering.GatheringService
import com.nbang.nbangapi.domain.share.SharedSettlementLinkService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class SharedSettlementFacade(
    private val sharedSettlementLinkService: SharedSettlementLinkService,
    private val gatheringService: GatheringService,
    private val calculationFacade: CalculationFacade,
    private val roundFacade: SettlementRoundFacade,
) {
    @Transactional
    fun createShareLink(gatheringId: Long, memberId: Long): SharedSettlementLinkResponse {
        gatheringService.findByIdAndValidateOwner(gatheringId, memberId)
        val link = sharedSettlementLinkService.create(gatheringId)
        return SharedSettlementLinkResponse(
            uuid = link.uuid,
            expiresAt = link.expiresAt.toString()
        )
    }

    fun getSharedSettlement(uuid: String): SharedSettlementResponse {
        val link = sharedSettlementLinkService.findByUuid(uuid)
        val gathering = gatheringService.findById(link.gatheringId)
        val calculation = calculationFacade.calculate(link.gatheringId)
        val rounds = roundFacade.findByGatheringId(link.gatheringId)

        return SharedSettlementResponse(
            gatheringName = gathering.name,
            gatheringType = gathering.type.name,
            totalAmount = calculation.totalAmount,
            balances = calculation.balances,
            debts = calculation.debts,
            rounds = rounds,
            expiresAt = link.expiresAt.toString()
        )
    }
}

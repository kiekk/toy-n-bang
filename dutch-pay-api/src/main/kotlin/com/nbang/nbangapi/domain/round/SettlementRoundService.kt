package com.nbang.nbangapi.domain.round

import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SettlementRoundService(
    private val roundRepository: SettlementRoundRepository
) {

    fun create(title: String, amount: BigDecimal, payerId: Long, gatheringId: Long): SettlementRound {
        return roundRepository.save(
            SettlementRound(
                title = title,
                amount = amount,
                payerId = payerId,
                gatheringId = gatheringId
            )
        )
    }

    fun findById(id: Long): SettlementRound {
        return roundRepository.findById(id)
            .orElseThrow { CoreException(ErrorType.ROUND_NOT_FOUND) }
    }

    fun findByGatheringId(gatheringId: Long): List<SettlementRound> {
        return roundRepository.findByGatheringId(gatheringId)
    }

    fun update(id: Long, title: String, amount: BigDecimal, payerId: Long): SettlementRound {
        val round = findById(id)
        round.update(title, amount, payerId)
        return round
    }

    fun updateReceiptImage(id: Long, imageUrl: String): SettlementRound {
        val round = findById(id)
        round.updateReceiptImageUrl(imageUrl)
        return round
    }

    fun delete(id: Long) {
        if (!roundRepository.existsById(id)) {
            throw CoreException(ErrorType.ROUND_NOT_FOUND)
        }
        roundRepository.deleteById(id)
    }

    fun existsById(id: Long): Boolean {
        return roundRepository.existsById(id)
    }
}

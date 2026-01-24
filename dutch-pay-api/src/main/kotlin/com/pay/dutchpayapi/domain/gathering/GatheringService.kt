package com.pay.dutchpayapi.domain.gathering

import com.pay.dutchpayapi.support.error.CoreException
import com.pay.dutchpayapi.support.error.ErrorType
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository
) {

    fun create(name: String, startDate: LocalDate, endDate: LocalDate): Gathering {
        return gatheringRepository.save(
            Gathering(
                name = name,
                startDate = startDate,
                endDate = endDate
            )
        )
    }

    fun findById(id: Long): Gathering {
        return gatheringRepository.findById(id)
            .orElseThrow { CoreException(ErrorType.GATHERING_NOT_FOUND) }
    }

    fun findAll(): List<Gathering> {
        return gatheringRepository.findAll()
    }

    fun update(id: Long, name: String, startDate: LocalDate, endDate: LocalDate): Gathering {
        val gathering = findById(id)
        gathering.update(name, startDate, endDate)
        return gathering
    }

    fun delete(id: Long) {
        if (!gatheringRepository.existsById(id)) {
            throw CoreException(ErrorType.GATHERING_NOT_FOUND)
        }
        gatheringRepository.deleteById(id)
    }

    fun existsById(id: Long): Boolean {
        return gatheringRepository.existsById(id)
    }
}

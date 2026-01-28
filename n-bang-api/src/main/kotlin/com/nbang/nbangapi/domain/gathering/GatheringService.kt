package com.nbang.nbangapi.domain.gathering

import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository
) {

    fun create(memberId: Long, name: String, startDate: LocalDate, endDate: LocalDate): Gathering {
        return gatheringRepository.save(
            Gathering(
                memberId = memberId,
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

    fun findByIdAndValidateOwner(id: Long, memberId: Long): Gathering {
        val gathering = findById(id)
        if (!gathering.isOwnedBy(memberId)) {
            throw CoreException(ErrorType.GATHERING_ACCESS_DENIED)
        }
        return gathering
    }

    fun findAllByMemberId(memberId: Long): List<Gathering> {
        return gatheringRepository.findAllByMemberId(memberId)
    }

    fun update(id: Long, memberId: Long, name: String, startDate: LocalDate, endDate: LocalDate): Gathering {
        val gathering = findByIdAndValidateOwner(id, memberId)
        gathering.update(name, startDate, endDate)
        return gathering
    }

    fun delete(id: Long, memberId: Long) {
        val gathering = findById(id)
        if (!gathering.isOwnedBy(memberId)) {
            throw CoreException(ErrorType.GATHERING_ACCESS_DENIED)
        }
        gatheringRepository.deleteById(id)
    }

    fun existsById(id: Long): Boolean {
        return gatheringRepository.existsById(id)
    }
}

package com.nbang.nbangapi.domain.share

import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SharedSettlementLinkService(
    private val repository: SharedSettlementLinkRepository
) {
    fun create(gatheringId: Long, expiresInHours: Long = 24): SharedSettlementLink {
        val link = SharedSettlementLink(
            gatheringId = gatheringId,
            expiresAt = LocalDateTime.now().plusHours(expiresInHours)
        )
        return repository.save(link)
    }

    fun findByUuid(uuid: String): SharedSettlementLink {
        val link = repository.findByUuid(uuid)
            .orElseThrow { CoreException(ErrorType.SHARED_LINK_NOT_FOUND) }
        if (link.isExpired()) {
            throw CoreException(ErrorType.SHARED_LINK_EXPIRED)
        }
        return link
    }
}

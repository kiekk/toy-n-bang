package com.nbang.nbangapi.infrastructure.share

import com.nbang.nbangapi.domain.share.SharedSettlementLink
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface SharedSettlementLinkJpaRepository : JpaRepository<SharedSettlementLink, Long> {
    fun findByUuid(uuid: String): Optional<SharedSettlementLink>
}

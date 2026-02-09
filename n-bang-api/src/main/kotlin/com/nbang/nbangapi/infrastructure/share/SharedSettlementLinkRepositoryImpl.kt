package com.nbang.nbangapi.infrastructure.share

import com.nbang.nbangapi.domain.share.SharedSettlementLink
import com.nbang.nbangapi.domain.share.SharedSettlementLinkRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class SharedSettlementLinkRepositoryImpl(
    private val jpaRepository: SharedSettlementLinkJpaRepository
) : SharedSettlementLinkRepository {

    override fun save(link: SharedSettlementLink): SharedSettlementLink {
        return jpaRepository.save(link)
    }

    override fun findByUuid(uuid: String): Optional<SharedSettlementLink> {
        return jpaRepository.findByUuid(uuid)
    }
}

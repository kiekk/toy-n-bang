package com.nbang.nbangapi.domain.share

import java.util.Optional

interface SharedSettlementLinkRepository {
    fun save(link: SharedSettlementLink): SharedSettlementLink
    fun findByUuid(uuid: String): Optional<SharedSettlementLink>
}

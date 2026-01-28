package com.nbang.nbangapi.domain.gathering

import java.util.*

interface GatheringRepository {
    fun save(gathering: Gathering): Gathering
    fun findAllByMemberId(memberId: Long): List<Gathering>
    fun findById(id: Long): Optional<Gathering>
    fun existsById(id: Long): Boolean
    fun deleteById(id: Long)
}

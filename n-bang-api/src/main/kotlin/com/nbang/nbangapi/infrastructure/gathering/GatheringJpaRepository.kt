package com.nbang.nbangapi.infrastructure.gathering

import com.nbang.nbangapi.domain.gathering.Gathering
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringJpaRepository : JpaRepository<Gathering, Long> {
    fun findAllByMemberId(memberId: Long): List<Gathering>
}

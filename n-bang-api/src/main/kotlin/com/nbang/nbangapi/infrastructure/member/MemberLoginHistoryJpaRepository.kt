package com.nbang.nbangapi.infrastructure.member

import com.nbang.nbangapi.domain.member.MemberLoginHistory
import org.springframework.data.jpa.repository.JpaRepository

interface MemberLoginHistoryJpaRepository : JpaRepository<MemberLoginHistory, Long> {
    fun findByMemberIdOrderByLoginAtDesc(memberId: Long): List<MemberLoginHistory>
}

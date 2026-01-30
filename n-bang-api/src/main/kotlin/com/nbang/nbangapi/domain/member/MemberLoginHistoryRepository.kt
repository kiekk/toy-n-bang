package com.nbang.nbangapi.domain.member

interface MemberLoginHistoryRepository {
    fun save(history: MemberLoginHistory): MemberLoginHistory
    fun findByMemberId(memberId: Long): List<MemberLoginHistory>
}

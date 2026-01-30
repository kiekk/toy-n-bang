package com.nbang.nbangapi.infrastructure.member

import com.nbang.nbangapi.domain.member.MemberLoginHistory
import com.nbang.nbangapi.domain.member.MemberLoginHistoryRepository
import org.springframework.stereotype.Repository

@Repository
class MemberLoginHistoryRepositoryImpl(
    private val memberLoginHistoryJpaRepository: MemberLoginHistoryJpaRepository,
) : MemberLoginHistoryRepository {

    override fun save(history: MemberLoginHistory): MemberLoginHistory {
        return memberLoginHistoryJpaRepository.save(history)
    }

    override fun findByMemberId(memberId: Long): List<MemberLoginHistory> {
        return memberLoginHistoryJpaRepository.findByMemberIdOrderByLoginAtDesc(memberId)
    }
}

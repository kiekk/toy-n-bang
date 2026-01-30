package com.nbang.nbangapi.interfaces.listener

import com.nbang.nbangapi.application.auth.event.MemberLoginEvent
import com.nbang.nbangapi.domain.member.MemberLoginHistory
import com.nbang.nbangapi.domain.member.MemberLoginHistoryRepository
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MemberLoginEventListener(
    private val memberLoginHistoryRepository: MemberLoginHistoryRepository,
) {

    @Async
    @EventListener
    fun handleMemberLoginEvent(event: MemberLoginEvent) {
        val history = MemberLoginHistory.create(
            memberId = event.memberId,
            provider = event.provider,
            ipAddress = event.ipAddress,
            userAgent = event.userAgent,
        )
        memberLoginHistoryRepository.save(history)
    }
}

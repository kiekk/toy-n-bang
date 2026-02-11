package com.nbang.nbangapi.interfaces.api.share

import com.nbang.nbangapi.application.share.SharedSettlementFacade
import com.nbang.nbangapi.application.share.response.SharedSettlementLinkResponse
import com.nbang.nbangapi.application.share.response.SharedSettlementResponse
import com.nbang.nbangapi.support.security.CurrentUser
import com.nbang.nbangapi.support.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class SharedSettlementController(
    private val sharedSettlementFacade: SharedSettlementFacade
) {
    @PostMapping("/api/gatherings/{gatheringId}/share")
    fun createShareLink(
        @CurrentUser user: UserPrincipal,
        @PathVariable gatheringId: Long
    ): ResponseEntity<SharedSettlementLinkResponse> {
        val response = sharedSettlementFacade.createShareLink(gatheringId, user.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/api/shared/{uuid}")
    fun getSharedSettlement(
        @PathVariable uuid: String
    ): ResponseEntity<SharedSettlementResponse> {
        val response = sharedSettlementFacade.getSharedSettlement(uuid)
        return ResponseEntity.ok(response)
    }
}

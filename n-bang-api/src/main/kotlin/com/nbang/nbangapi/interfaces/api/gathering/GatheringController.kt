package com.nbang.nbangapi.interfaces.api.gathering

import com.nbang.nbangapi.application.gathering.GatheringFacade
import com.nbang.nbangapi.application.gathering.request.GatheringCreateRequest
import com.nbang.nbangapi.application.gathering.request.GatheringUpdateRequest
import com.nbang.nbangapi.application.gathering.response.GatheringResponse
import com.nbang.nbangapi.support.security.CurrentUser
import com.nbang.nbangapi.support.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/gatherings")
class GatheringController(
    private val gatheringFacade: GatheringFacade
) {

    @PostMapping
    fun create(
        @CurrentUser user: UserPrincipal,
        @RequestBody request: GatheringCreateRequest
    ): ResponseEntity<GatheringResponse> {
        val response = gatheringFacade.create(user.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun findAll(@CurrentUser user: UserPrincipal): ResponseEntity<List<GatheringResponse>> {
        val responses = gatheringFacade.findAllByMemberId(user.id)
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/{id}")
    fun findById(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long
    ): ResponseEntity<GatheringResponse> {
        val response = gatheringFacade.findById(id, user.id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}")
    fun update(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
        @RequestBody request: GatheringUpdateRequest
    ): ResponseEntity<GatheringResponse> {
        val response = gatheringFacade.update(id, user.id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        gatheringFacade.delete(id, user.id)
        return ResponseEntity.noContent().build()
    }
}

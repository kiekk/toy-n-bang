package com.pay.dutchpayapi.interfaces.api.gathering

import com.pay.dutchpayapi.application.gathering.GatheringCreateRequest
import com.pay.dutchpayapi.application.gathering.GatheringResponse
import com.pay.dutchpayapi.application.gathering.GatheringService
import com.pay.dutchpayapi.application.gathering.GatheringUpdateRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/gatherings")
class GatheringController(
    private val gatheringService: GatheringService
) {

    @PostMapping
    fun create(@RequestBody request: GatheringCreateRequest): ResponseEntity<GatheringResponse> {
        val response = gatheringService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<GatheringResponse>> {
        val responses = gatheringService.findAll()
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<GatheringResponse> {
        val response = gatheringService.findById(id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: GatheringUpdateRequest
    ): ResponseEntity<GatheringResponse> {
        val response = gatheringService.update(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        gatheringService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

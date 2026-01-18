package com.pay.dutchpayapi.interfaces.api.round

import com.pay.dutchpayapi.application.round.RoundCreateRequest
import com.pay.dutchpayapi.application.round.RoundResponse
import com.pay.dutchpayapi.application.round.RoundUpdateRequest
import com.pay.dutchpayapi.application.round.SettlementRoundService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api")
class SettlementRoundController(
    private val roundService: SettlementRoundService
) {

    @PostMapping("/gatherings/{gatheringId}/rounds")
    fun create(
        @PathVariable gatheringId: UUID,
        @RequestBody request: RoundCreateRequest
    ): ResponseEntity<RoundResponse> {
        val response = roundService.create(gatheringId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/gatherings/{gatheringId}/rounds")
    fun findByGatheringId(@PathVariable gatheringId: UUID): ResponseEntity<List<RoundResponse>> {
        val responses = roundService.findByGatheringId(gatheringId)
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/rounds/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<RoundResponse> {
        val response = roundService.findById(id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/rounds/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: RoundUpdateRequest
    ): ResponseEntity<RoundResponse> {
        val response = roundService.update(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/rounds/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        roundService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/rounds/{id}/image")
    fun uploadImage(
        @PathVariable id: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<RoundResponse> {
        val imageUrl = "/uploads/${file.originalFilename}"
        val response = roundService.updateReceiptImage(id, imageUrl)
        return ResponseEntity.ok(response)
    }
}

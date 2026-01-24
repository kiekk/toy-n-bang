package com.pay.dutchpayapi.interfaces.api.round

import com.pay.dutchpayapi.application.round.SettlementRoundFacade
import com.pay.dutchpayapi.application.round.request.RoundCreateRequest
import com.pay.dutchpayapi.application.round.request.RoundUpdateRequest
import com.pay.dutchpayapi.application.round.response.RoundResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api")
class SettlementRoundController(
    private val roundFacade: SettlementRoundFacade
) {

    @PostMapping("/gatherings/{gatheringId}/rounds")
    fun create(
        @PathVariable gatheringId: Long,
        @RequestBody request: RoundCreateRequest
    ): ResponseEntity<RoundResponse> {
        val response = roundFacade.create(gatheringId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/gatherings/{gatheringId}/rounds")
    fun findByGatheringId(@PathVariable gatheringId: Long): ResponseEntity<List<RoundResponse>> {
        val responses = roundFacade.findByGatheringId(gatheringId)
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/rounds/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<RoundResponse> {
        val response = roundFacade.findById(id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/rounds/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: RoundUpdateRequest
    ): ResponseEntity<RoundResponse> {
        val response = roundFacade.update(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/rounds/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        roundFacade.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/rounds/{id}/image")
    fun uploadImage(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<RoundResponse> {
        val imageUrl = "/uploads/${file.originalFilename}"
        val response = roundFacade.updateReceiptImage(id, imageUrl)
        return ResponseEntity.ok(response)
    }
}

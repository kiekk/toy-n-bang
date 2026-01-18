package com.pay.dutchpayapi.interfaces.api.participant

import com.pay.dutchpayapi.application.participant.ParticipantCreateRequest
import com.pay.dutchpayapi.application.participant.ParticipantResponse
import com.pay.dutchpayapi.application.participant.ParticipantService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api")
class ParticipantController(
    private val participantService: ParticipantService
) {

    @PostMapping("/gatherings/{gatheringId}/participants")
    fun addToGathering(
        @PathVariable gatheringId: UUID,
        @RequestBody request: ParticipantCreateRequest
    ): ResponseEntity<ParticipantResponse> {
        val response = participantService.addToGathering(gatheringId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/gatherings/{gatheringId}/participants")
    fun findByGatheringId(@PathVariable gatheringId: UUID): ResponseEntity<List<ParticipantResponse>> {
        val responses = participantService.findByGatheringId(gatheringId)
        return ResponseEntity.ok(responses)
    }

    @DeleteMapping("/participants/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        participantService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

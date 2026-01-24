package com.pay.dutchpayapi.interfaces.api.participant

import com.pay.dutchpayapi.application.participant.ParticipantFacade
import com.pay.dutchpayapi.application.participant.request.ParticipantCreateRequest
import com.pay.dutchpayapi.application.participant.response.ParticipantResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ParticipantController(
    private val participantFacade: ParticipantFacade
) {

    @PostMapping("/gatherings/{gatheringId}/participants")
    fun addToGathering(
        @PathVariable gatheringId: Long,
        @RequestBody request: ParticipantCreateRequest
    ): ResponseEntity<ParticipantResponse> {
        val response = participantFacade.addToGathering(gatheringId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/gatherings/{gatheringId}/participants")
    fun findByGatheringId(@PathVariable gatheringId: Long): ResponseEntity<List<ParticipantResponse>> {
        val responses = participantFacade.findByGatheringId(gatheringId)
        return ResponseEntity.ok(responses)
    }

    @DeleteMapping("/participants/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        participantFacade.delete(id)
        return ResponseEntity.noContent().build()
    }
}

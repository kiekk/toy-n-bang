package com.nbang.nbangapi.interfaces.api.gathering

import com.nbang.nbangapi.application.gathering.GatheringFacade
import com.nbang.nbangapi.application.gathering.request.GatheringCreateRequest
import com.nbang.nbangapi.application.gathering.request.GatheringUpdateRequest
import com.nbang.nbangapi.application.gathering.response.GatheringResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/gatherings")
class GatheringController(
    private val gatheringFacade: GatheringFacade
) {

    @PostMapping
    fun create(@RequestBody request: GatheringCreateRequest): ResponseEntity<GatheringResponse> {
        val response = gatheringFacade.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<GatheringResponse>> {
        val responses = gatheringFacade.findAll()
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<GatheringResponse> {
        val response = gatheringFacade.findById(id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: GatheringUpdateRequest
    ): ResponseEntity<GatheringResponse> {
        val response = gatheringFacade.update(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        gatheringFacade.delete(id)
        return ResponseEntity.noContent().build()
    }
}

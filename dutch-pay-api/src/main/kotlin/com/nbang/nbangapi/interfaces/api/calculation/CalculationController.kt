package com.nbang.nbangapi.interfaces.api.calculation

import com.nbang.nbangapi.application.calculation.CalculationFacade
import com.nbang.nbangapi.application.calculation.response.CalculationResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/gatherings")
class CalculationController(
    private val calculationFacade: CalculationFacade
) {

    @GetMapping("/{id}/calculate")
    fun calculate(@PathVariable id: Long): ResponseEntity<CalculationResponse> {
        val response = calculationFacade.calculate(id)
        return ResponseEntity.ok(response)
    }
}

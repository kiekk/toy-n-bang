package com.pay.dutchpayapi.interfaces.api.calculation

import com.pay.dutchpayapi.application.calculation.CalculationResponse
import com.pay.dutchpayapi.application.calculation.CalculationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/gatherings")
class CalculationController(
    private val calculationService: CalculationService
) {

    @GetMapping("/{id}/calculate")
    fun calculate(@PathVariable id: UUID): ResponseEntity<CalculationResponse> {
        val response = calculationService.calculate(id)
        return ResponseEntity.ok(response)
    }
}

package com.pay.dutchpayapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class DutchPayApiApplication

fun main(args: Array<String>) {
    runApplication<DutchPayApiApplication>(*args)
}

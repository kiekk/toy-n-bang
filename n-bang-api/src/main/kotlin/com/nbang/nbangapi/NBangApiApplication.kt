package com.nbang.nbangapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
class NBangApiApplication

fun main(args: Array<String>) {
    runApplication<NBangApiApplication>(*args)
}

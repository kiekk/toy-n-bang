package com.pay.dutchpayapi

import com.pay.dutchpayapi.support.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class DutchPayApiApplicationTests {

    @Test
    fun contextLoads() {
    }
}

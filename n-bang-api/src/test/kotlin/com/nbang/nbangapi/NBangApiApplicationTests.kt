package com.nbang.nbangapi

import com.nbang.nbangapi.support.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class NBangApiApplicationTests {

    @Test
    fun contextLoads() {
    }
}

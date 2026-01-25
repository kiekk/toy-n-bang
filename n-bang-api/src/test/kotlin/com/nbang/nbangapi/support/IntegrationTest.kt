package com.nbang.nbangapi.support

import com.nbang.nbangapi.support.utils.DatabaseCleanUp
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
abstract class IntegrationTest {

    @Autowired
    private lateinit var databaseCleanUp: DatabaseCleanUp

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }
}

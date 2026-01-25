package com.nbang.nbangapi.support.logging

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class LoggingConfig {

    /**
     * 비동기 작업에서 MDC 컨텍스트를 유지하기 위한 TaskExecutor
     */
    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 25
        executor.setThreadNamePrefix("async-")
        executor.setTaskDecorator(MdcTaskDecorator())
        executor.initialize()
        return executor
    }
}

package com.nbang.nbangapi.support.logging

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator

/**
 * 비동기 작업에서 MDC 컨텍스트를 전파하기 위한 TaskDecorator
 * @Async 사용 시 MDC 컨텍스트가 유지되도록 합니다.
 */
class MdcTaskDecorator : TaskDecorator {

    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()

        return Runnable {
            try {
                contextMap?.let { MDC.setContextMap(it) }
                runnable.run()
            } finally {
                MDC.clear()
            }
        }
    }
}

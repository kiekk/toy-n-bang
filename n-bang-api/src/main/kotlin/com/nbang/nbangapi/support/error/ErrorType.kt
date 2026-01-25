package com.nbang.nbangapi.support.error

import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: String, val message: String) {
    /** 범용 에러 */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.reasonPhrase, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.reasonPhrase, "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.reasonPhrase, "이미 존재하는 리소스입니다."),

    /** 모임 관련 에러 */
    GATHERING_NOT_FOUND(HttpStatus.NOT_FOUND, "GATHERING_NOT_FOUND", "모임을 찾을 수 없습니다."),

    /** 참여자 관련 에러 */
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT_NOT_FOUND", "참여자를 찾을 수 없습니다."),

    /** 정산 라운드 관련 에러 */
    ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUND_NOT_FOUND", "정산 라운드를 찾을 수 없습니다."),
}

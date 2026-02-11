package com.nbang.nbangapi.support.error

import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: String, val message: String) {
    /** 범용 에러 */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.reasonPhrase, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.reasonPhrase, "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.reasonPhrase, "이미 존재하는 리소스입니다."),

    /** 인증/인가 관련 에러 */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다."),

    /** 회원 관련 에러 */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),

    /** 모임 관련 에러 */
    GATHERING_NOT_FOUND(HttpStatus.NOT_FOUND, "GATHERING_NOT_FOUND", "모임을 찾을 수 없습니다."),
    GATHERING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "GATHERING_ACCESS_DENIED", "해당 모임에 접근 권한이 없습니다."),

    /** 참여자 관련 에러 */
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT_NOT_FOUND", "참여자를 찾을 수 없습니다."),

    /** 정산 라운드 관련 에러 */
    ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUND_NOT_FOUND", "정산 라운드를 찾을 수 없습니다."),

    /** 공유 링크 관련 에러 */
    SHARED_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "SHARED_LINK_NOT_FOUND", "공유 링크를 찾을 수 없습니다."),
    SHARED_LINK_EXPIRED(HttpStatus.valueOf(410), "SHARED_LINK_EXPIRED", "만료된 공유 링크입니다."),
}

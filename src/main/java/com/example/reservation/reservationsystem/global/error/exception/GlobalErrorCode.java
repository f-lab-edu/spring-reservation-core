package com.example.reservation.reservationsystem.global.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    INTERNAL_SERVER_EXCEPTION("G001", "알 수 없는 오류 입니다.", 500),
    INVALID_INPUT_VALUE("G002", "잘못된 입력 값입니다.", 400),
    UNEXPECTED_RUNTIME_EXCEPTION("G003", "서버 내부 오류 입니다.", 500),
    CHECKED_EXCEPTION("G004", "일시적인 서버 오류 입니다.", 500);

    private final String code;
    private final String message;
    private final int status;

}

package com.example.reservation.reservationsystem.global.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    INTERNAL_SERVER_EXCEPTION("G001", "알 수 없는 오류 입니다.", 500);

    private final String code;
    private final String message;
    private final int status;

}

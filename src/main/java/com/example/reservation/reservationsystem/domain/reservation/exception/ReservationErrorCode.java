package com.example.reservation.reservationsystem.domain.reservation.exception;

import com.example.reservation.reservationsystem.global.error.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode implements ErrorCode {
    RESERVATION_ALREADY_CANCELLED("R001", "이미 취소된 예약입니다.", HttpStatus.BAD_REQUEST.value()),
    RESERVATION_NOT_FOUND("R002", "존재하지 않는 예약입니다.", HttpStatus.NOT_FOUND.value());

    private final String code;
    private final String message;
    private final int status;
}

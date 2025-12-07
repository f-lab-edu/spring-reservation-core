package com.example.reservation.reservationsystem.domain.slot.exception;

import com.example.reservation.reservationsystem.global.error.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlotErrorCode implements ErrorCode {
    SLOT_NOT_RESERVABLE("S001", "예약할 수 없습니다.", 409),
    SLOT_ALREADY_FULL("S002", "슬롯이 이미 꽉 찼습니다.", 409),
    SLOT_NOT_FOUND("S003", "슬롯을 찾을 수 없습니다.", 404);

    private final String code;
    private final String message;
    private final int status;
}

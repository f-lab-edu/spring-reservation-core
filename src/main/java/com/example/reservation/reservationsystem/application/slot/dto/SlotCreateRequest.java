package com.example.reservation.reservationsystem.application.slot.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record SlotCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotNull(message = "시작 시간은 필수입니다.") @Future(message = "시작 시간은 현재 시간 이후여야 합니다.")
        LocalDateTime startAt,

        @NotNull(message = "종료 시간은 필수입니다.") @Future(message = "종료 시간은 현재 시간 이후여야 합니다.")
        LocalDateTime endAt,

        @NotNull(message = "수용 인원은 필수입니다.") @Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다.")
        Integer capacity
) {
}

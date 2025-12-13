package com.example.reservation.reservationsystem.common.validation.timerange;

import java.time.LocalDateTime;

public interface TimeRange {
    LocalDateTime startAt();

    LocalDateTime endAt();
}

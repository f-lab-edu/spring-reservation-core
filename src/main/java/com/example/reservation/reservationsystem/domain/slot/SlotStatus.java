package com.example.reservation.reservationsystem.domain.slot;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SlotStatus {
    OPEN("예약 가능한 상태"),
    CLOSED("예약이 불가능한 상태");

    private final String description;
}

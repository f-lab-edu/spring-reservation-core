package com.example.reservation.reservationsystem.application.slot.dto;

import com.example.reservation.reservationsystem.domain.slot.Slot;
import com.example.reservation.reservationsystem.domain.slot.SlotStatus;
import java.time.LocalDateTime;

public record SlotResponse(
        Long id,
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer capacity,
        Integer remaining,
        SlotStatus status) {
    public static SlotResponse from(Slot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getTitle(),
                slot.getStartAt(),
                slot.getEndAt(),
                slot.getCapacity(),
                slot.getRemaining(),
                slot.getStatus());
    }
}

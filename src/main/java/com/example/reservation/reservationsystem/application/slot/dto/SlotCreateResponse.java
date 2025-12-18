package com.example.reservation.reservationsystem.application.slot.dto;

public record SlotCreateResponse(Long slotId) {
    public static SlotCreateResponse from(Long slotId) {
        return new SlotCreateResponse(slotId);
    }
}

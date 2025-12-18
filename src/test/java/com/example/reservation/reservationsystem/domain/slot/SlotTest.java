package com.example.reservation.reservationsystem.domain.slot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class SlotTest {

    @Test
    @DisplayName("예약 가능 확인 - 성공")
    void ensureReservable_success() {
        // given
        Slot slot = Slot.builder()
                .capacity(10)
                .remaining(1)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(1))
                .status(SlotStatus.OPEN)
                .build();

        // when & then
        slot.ensureReservable(); // Should not throw
    }
}

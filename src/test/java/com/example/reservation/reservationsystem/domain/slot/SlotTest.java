package com.example.reservation.reservationsystem.domain.slot;

import com.example.reservation.reservationsystem.domain.slot.exception.SlotErrorCode;
import com.example.reservation.reservationsystem.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotTest {

    @Test
    @DisplayName("잔여 수량 감소 성공 (1시간 동안의 이벤트)")
    void decreaseRemaining_success() {
        // given
        Slot slot = Slot.builder()
                .capacity(10)
                .remaining(10)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(1))
                .status(SlotStatus.OPEN)
                .build();

        // when
        slot.decreaseRemaining();

        // then
        assertThat(slot.getRemaining()).isEqualTo(9);
    }

    @Test
    @DisplayName("잔여 수량 감소 실패 - OPEN 상태 아님")
    void decreaseRemaining_fail_not_open() {
        // given
        Slot slot = Slot.builder()
                .capacity(10)
                .remaining(10)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(1))
                .status(SlotStatus.CLOSED)
                .build();

        // when & then
        assertThatThrownBy(() -> slot.decreaseRemaining())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SlotErrorCode.SLOT_NOT_RESERVABLE);
    }

    @Test
    @DisplayName("잔여 수량 감소 실패 - 잔여 수량 0")
    void decreaseRemaining_fail_no_remaining() {
        // given
        Slot slot = Slot.builder()
                .capacity(10)
                .remaining(0)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(1))
                .status(SlotStatus.OPEN)
                .build();

        // when & then
        assertThatThrownBy(() -> slot.decreaseRemaining())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SlotErrorCode.SLOT_NOT_RESERVABLE);
    }

    @Test
    @DisplayName("잔여 수량 증가 성공")
    void increaseRemaining_success() {
        // given
        Slot slot = Slot.builder()
                .capacity(10)
                .remaining(5)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(1))
                .status(SlotStatus.OPEN)
                .build();

        // when
        slot.increaseRemaining();

        // then
        assertThat(slot.getRemaining()).isEqualTo(6);
    }

    @Test
    @DisplayName("잔여 수량 증가 실패 - Capacity 초과")
    void increaseRemaining_fail_capacity_full() {
        // given
        Slot slot = Slot.builder()
                .capacity(10)
                .remaining(10)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(1))
                .status(SlotStatus.OPEN)
                .build();

        // when & then
        assertThatThrownBy(() -> slot.increaseRemaining())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SlotErrorCode.SLOT_ALREADY_FULL);
    }

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

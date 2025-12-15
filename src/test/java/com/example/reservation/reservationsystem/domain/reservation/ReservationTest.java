package com.example.reservation.reservationsystem.domain.reservation;

import com.example.reservation.reservationsystem.domain.reservation.exception.ReservationErrorCode;
import com.example.reservation.reservationsystem.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.ToStringExclude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    @Test
    @DisplayName("예약 성공")
    void create_success() {
        //given
        Reservation reservation = Reservation.builder()
                .slotId(1L)
                .userId(1L)
                .build();

        //when


        //then
        assertThat(reservation.getSlotId()).isEqualTo(1L);
        assertThat(reservation.getUserId()).isEqualTo(1L);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }


    @Test
    @DisplayName("예약 취소 성공")
    void cancel_success() {
        // given
        Reservation reservation = Reservation.builder()
                .slotId(1L)
                .userId(1L)
                .build();

        // when
        reservation.cancel();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    @DisplayName("이미 취소된 예약 취소 시도")
    void cancel_fail_already_cancelled() {
        // given
        Reservation reservation = Reservation.builder()
                .slotId(1L)
                .userId(1L)
                .build();

        // when
        reservation.cancel();

        // then
        assertThatThrownBy(() -> reservation.cancel())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.RESERVATION_ALREADY_CANCELLED);
    }
}

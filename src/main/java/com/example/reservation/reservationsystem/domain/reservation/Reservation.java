package com.example.reservation.reservationsystem.domain.reservation;

import com.example.reservation.reservationsystem.domain.common.BaseEntity;
import com.example.reservation.reservationsystem.domain.reservation.exception.ReservationErrorCode;
import com.example.reservation.reservationsystem.global.error.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long slotId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Builder
    public Reservation(Long slotId, Long userId) {
        this(slotId, userId, ReservationStatus.CONFIRMED);
    }

    private Reservation(Long slotId, Long userId, ReservationStatus status) {
        this.slotId = slotId;
        this.userId = userId;
        this.status = status;
    }

    public void cancel() {
        if (this.status == ReservationStatus.CANCELLED) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_ALREADY_CANCELLED);
        }
        this.status = ReservationStatus.CANCELLED;
    }
}

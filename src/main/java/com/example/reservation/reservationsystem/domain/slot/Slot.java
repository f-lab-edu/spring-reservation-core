package com.example.reservation.reservationsystem.domain.slot;

import com.example.reservation.reservationsystem.domain.common.BaseEntity;
import com.example.reservation.reservationsystem.domain.slot.exception.SlotErrorCode;
import com.example.reservation.reservationsystem.global.error.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예약 가능한 시간과 재고를 가진 상품 도메인
 * ex)
 * - 콘서트 티켓 예매
 * - 병원 진료 예약
 * - 회의실 예약
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "slots", indexes = {
    @Index(name = "idx_slot_time_range", columnList = "startAt, endAt")
})
public class Slot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer remaining;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;

    @Builder
    public Slot(String title, LocalDateTime startAt, LocalDateTime endAt, Integer capacity) {
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.capacity = capacity;
        this.remaining = capacity;
        this.status = SlotStatus.OPEN;
    }

    /**
     * remaining 감소 (예약 발생 시)
     * - 예약 가능한 상태(reservable)가 아니면 예외 발생
     */
    public void decreaseRemaining() {
        if (!isReservable()) {
            throw new BusinessException(SlotErrorCode.SLOT_NOT_RESERVABLE);
        }
        this.remaining--;
    }

    /**
     * remaining 증가 (예약 취소 시)
     * - 처음 설정한 capacity를 초과하면 예외 발생
     */
    public void increaseRemaining() {
        if (this.remaining >= this.capacity) {
            throw new BusinessException(SlotErrorCode.SLOT_ALREADY_FULL);
        }
        this.remaining++;
    }

    /**
     * 예약 가능 여부 검증 : 외부 호출 (isReservable은 private)
     * - 불가능하면 예외 발생
     */
    public void ensureReservable() {
        if (!isReservable()) {
            throw new BusinessException(SlotErrorCode.SLOT_NOT_RESERVABLE);
        }
    }

    /**
     * 내부 예약 가능 여부 체크 로직
     * - 상태가 OPEN이고, 잔여량이 0보다 커야 함
     */
    private boolean isReservable() {
        return this.status == SlotStatus.OPEN && this.remaining > 0;
    }
}

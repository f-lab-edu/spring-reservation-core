package com.example.reservation.reservationsystem.domain.slot;

import com.example.reservation.reservationsystem.application.slot.dto.SlotCreateRequest;
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
    public Slot(String title, LocalDateTime startAt, LocalDateTime endAt, Integer capacity, Integer remaining, SlotStatus status) {
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.capacity = capacity;
        this.remaining = remaining != null ? remaining : capacity;
        this.status = status != null ? status : SlotStatus.OPEN;
    }

    public static Slot of(SlotCreateRequest request) {
        return Slot.builder()
                .title(request.title())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .capacity(request.capacity())
                .remaining(request.capacity())
                .status(SlotStatus.OPEN)
                .build();
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

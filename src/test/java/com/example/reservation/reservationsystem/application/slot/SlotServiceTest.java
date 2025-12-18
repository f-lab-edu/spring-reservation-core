package com.example.reservation.reservationsystem.application.slot;

import com.example.reservation.reservationsystem.application.slot.dto.SlotCreateRequest;
import com.example.reservation.reservationsystem.application.slot.dto.SlotResponse;
import com.example.reservation.reservationsystem.domain.slot.Slot;
import com.example.reservation.reservationsystem.domain.slot.SlotStatus;
import com.example.reservation.reservationsystem.domain.slot.exception.SlotErrorCode;
import com.example.reservation.reservationsystem.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@DisplayName("슬롯 서비스 테스트")
class SlotServiceTest extends AbstractSlotServiceTest {

    private final String SLOT_NAME = "test slot";
    private final int DEFAULT_CAPACITY = 10;

    @Test
    @DisplayName("Slot 생성")
    void createSlot() {
        // given
        SlotCreateRequest request = new SlotCreateRequest(
                SLOT_NAME,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                DEFAULT_CAPACITY);

        // when
        Long slotId = slotService.createSlot(request).slotId();

        // then
        Slot slot = slotRepository.findById(slotId).orElseThrow();
        assertThat(slot.getTitle()).isEqualTo(SLOT_NAME);
        assertThat(slot.getRemaining()).isEqualTo(DEFAULT_CAPACITY);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.OPEN);
    }

    @Test
    @DisplayName("Slot 단건 조회")
    void getSlot() {
        // given
        Slot slot = Slot.builder()
                .title(SLOT_NAME)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(DEFAULT_CAPACITY)
                .remaining(DEFAULT_CAPACITY)
                .status(SlotStatus.OPEN)
                .build();

        Slot savedSlot = slotRepository.save(slot);

        // when
        SlotResponse response = slotService.getSlot(savedSlot.getId());

        // then
        assertThat(response.id()).isEqualTo(savedSlot.getId());
        assertThat(response.title()).isEqualTo(SLOT_NAME);
    }

    @Test
    @DisplayName("Slot 단건 조회 실패 시 S003 코드를 반환한다.")
    void getSlotNotFound() {
        // expected
        assertThatThrownBy(() -> slotService.getSlot(100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SlotErrorCode.SLOT_NOT_FOUND);
    }

    @Test
    @DisplayName("Slot 리스트 조회")
    void getSlots() {
        // given
        final int slotSize = 15;

        for (int i = 1; i <= slotSize; i++) {
            slotRepository.save(Slot.builder()
                    .title("Slot " + i)
                    .startAt(LocalDateTime.now().plusDays(1))
                    .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                    .capacity(DEFAULT_CAPACITY)
                    .remaining(DEFAULT_CAPACITY)
                    .status(SlotStatus.OPEN)
                    .build());
        }

        // when
        final int offset = 0;
        final int limit = 10;
        final int totalPage = (int) Math.ceil(((double) slotSize / limit));

        Pageable pageable = PageRequest.of(offset, limit);

        Page<SlotResponse> page = slotService.getSlots(pageable);

        // then
        assertThat(page.getTotalElements()).isEqualTo(slotSize);
        assertThat(page.getContent()).hasSize(limit);
        assertThat(page.getNumber()).isEqualTo(offset);
        assertThat(page.getTotalPages()).isEqualTo(totalPage);
    }

    @Test
    @DisplayName("Slot 예약 성공")
    void reserveSlot() {
        // given
        Slot slot = Slot.builder()
                .title(SLOT_NAME)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(10)
                .remaining(10)
                .status(SlotStatus.OPEN)
                .build();
        Slot savedSlot = slotRepository.save(slot);

        // when
        slotService.reserveSlot(savedSlot.getId());

        // then
        Slot updatedSlot = slotRepository.findById(savedSlot.getId()).orElseThrow();
        assertThat(updatedSlot.getRemaining()).isEqualTo(9);
    }

    @Test
    @DisplayName("Slot 예약 실패(잔여 수량 부족)시 S001 코드를 반환한다.")
    void reserveSlotFail() {
        // given
        Slot slot = Slot.builder()
                .title(SLOT_NAME)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(10)
                .remaining(1)
                .status(SlotStatus.OPEN)
                .build();
        Slot savedSlot = slotRepository.save(slot);
        slotService.reserveSlot(savedSlot.getId());

        // expected
        assertThatThrownBy(() -> slotService.reserveSlot(savedSlot.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SlotErrorCode.SLOT_NOT_RESERVABLE);
    }

    @Test
    @DisplayName("Slot 예약 취소 성공")
    void cancelSlotReservation() {
        // given
        Slot slot = Slot.builder()
                .title(SLOT_NAME)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(10)
                .remaining(9)
                .status(SlotStatus.OPEN)
                .build();
        Slot savedSlot = slotRepository.save(slot);
        slotService.reserveSlot(savedSlot.getId());

        // when
        slotService.cancelSlotReservation(savedSlot.getId());

        // then
        Slot updatedSlot = slotRepository.findById(savedSlot.getId()).orElseThrow();
        assertThat(updatedSlot.getRemaining()).isEqualTo(9);
    }
}

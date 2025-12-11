package com.example.reservation.reservationsystem.domain.slot;

import com.example.reservation.reservationsystem.infrastructure.slot.SlotMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Slot Repository Test (Memory)")
class SlotRepositoryTest {

    private SlotRepository slotRepository;

    @BeforeEach
    void setUp() {
        slotRepository = new SlotMemoryRepository(new ConcurrentHashMap<>());
    }

    @Test
    @DisplayName("Slot 저장 및 조회")
    void saveAndFind() {
        // given
        Slot slot = Slot.builder()
                .title("테스트 슬롯")
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(1))
                .capacity(10)
                .remaining(10)
                .status(SlotStatus.OPEN)
                .build();

        // when
        Slot savedSlot = slotRepository.save(slot);
        Slot foundSlot = slotRepository.findById(savedSlot.getId()).orElse(null);

        // then
        assertThat(foundSlot).isNotNull();
        assertThat(foundSlot.getId()).isEqualTo(savedSlot.getId());
        assertThat(foundSlot.getTitle()).isEqualTo("테스트 슬롯");
    }
}

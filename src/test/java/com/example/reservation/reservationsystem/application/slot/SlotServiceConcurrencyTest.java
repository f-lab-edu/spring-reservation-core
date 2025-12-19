package com.example.reservation.reservationsystem.application.slot;

import com.example.reservation.reservationsystem.domain.slot.Slot;
import com.example.reservation.reservationsystem.domain.slot.SlotStatus;
import com.example.reservation.reservationsystem.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("슬롯 서비스 동시성 테스트")
public class SlotServiceConcurrencyTest extends AbstractSlotServiceTest {
    @Test
    @DisplayName("Slot 예약 동시성 테스트 - 10개 잔여에 100명 동시 요청")
    void reserveSlotConcurrency() throws InterruptedException {
        // given
        final int capacity = 10;
        final int users = 100;
        Slot slot = Slot.builder()
                .title("슬롯 동시성 테스트")
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(capacity)
                .remaining(capacity)
                .status(SlotStatus.OPEN)
                .build();
        Slot savedSlot = slotRepository.save(slot);

        ExecutorService executorService = Executors.newFixedThreadPool(users);
        CountDownLatch latch = new CountDownLatch(users);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < users; i++) {
            executorService.submit(() -> {
                try {
                    start.await();
                    slotService.reserveSlot(savedSlot.getId());
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        start.countDown();
        latch.await();

        // then
        Slot updatedSlot = slotRepository.findById(savedSlot.getId()).orElseThrow();
        assertThat(updatedSlot.getRemaining()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(capacity);
        assertThat(failCount.get()).isEqualTo(users - capacity);
    }
}

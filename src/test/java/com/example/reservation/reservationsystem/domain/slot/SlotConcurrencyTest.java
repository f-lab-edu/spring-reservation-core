package com.example.reservation.reservationsystem.domain.slot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SlotConcurrencyTest {

    @Test
    @DisplayName("동시성 테스트: 100명이 동시에 1개씩 차감 (실패 에상 테스트)")
    void decreaseRemaining_concurrency_fail() throws InterruptedException {
        // given
        int threadCount = 100;
        Slot slot = Slot.builder()
                .capacity(100)
                .remaining(100)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(1))
                .status(SlotStatus.OPEN)
                .build();

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    slot.decreaseRemaining();
                } catch (InterruptedException e) {
                    //InterruptedException => interrupt flag changed to false
                    //re-interrupt interrupt 여부를 남기기 위함.
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        startLatch.countDown();
        latch.await();

        // then
        System.out.println("Remaining Slots: " + slot.getRemaining());
    }
}

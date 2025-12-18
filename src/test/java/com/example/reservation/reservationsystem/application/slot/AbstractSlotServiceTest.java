package com.example.reservation.reservationsystem.application.slot;

import com.example.reservation.reservationsystem.infrastructure.slot.SlotJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class AbstractSlotServiceTest {
    @Autowired
    protected SlotService slotService;
    @Autowired
    protected SlotJpaRepository slotRepository;
}

package com.example.reservation.reservationsystem.infrastructure.slot;

import com.example.reservation.reservationsystem.domain.slot.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotJpaRepository extends JpaRepository<Slot, Long> {
}

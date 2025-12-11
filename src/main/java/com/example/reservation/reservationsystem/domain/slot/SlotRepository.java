package com.example.reservation.reservationsystem.domain.slot;

import java.util.Optional;

public interface SlotRepository {
    Slot save(Slot slot);
    Optional<Slot> findById(Long id);
}

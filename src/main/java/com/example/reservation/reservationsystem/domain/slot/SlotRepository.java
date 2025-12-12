package com.example.reservation.reservationsystem.domain.slot;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SlotRepository {
    Slot save(Slot slot);

    Optional<Slot> findById(Long id);

    Page<Slot> findAll(Pageable pageable);
}

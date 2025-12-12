package com.example.reservation.reservationsystem.infrastructure.slot;

import com.example.reservation.reservationsystem.domain.slot.Slot;
import com.example.reservation.reservationsystem.domain.slot.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SlotRepositoryImpl implements SlotRepository {

    private final SlotJpaRepository slotJpaRepository;

    @Override
    public Slot save(Slot slot) {
        return slotJpaRepository.save(slot);
    }

    @Override
    public Optional<Slot> findById(Long id) {
        return slotJpaRepository.findById(id);
    }

    @Override
    public Page<Slot> findAll(Pageable pageable) {
        return slotJpaRepository.findAll(pageable);
    }
}

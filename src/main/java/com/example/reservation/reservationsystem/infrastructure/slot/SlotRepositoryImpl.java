package com.example.reservation.reservationsystem.infrastructure.slot;

import com.example.reservation.reservationsystem.domain.slot.Slot;
import com.example.reservation.reservationsystem.domain.slot.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}

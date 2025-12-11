package com.example.reservation.reservationsystem.infrastructure.slot;

import com.example.reservation.reservationsystem.domain.slot.Slot;
import com.example.reservation.reservationsystem.domain.slot.SlotRepository;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class SlotMemoryRepository implements SlotRepository {
    private final Map<Long, Slot> store;
    private final AtomicLong sequence;

    public SlotMemoryRepository(Map<Long, Slot> store) {
        this.store = store;
        this.sequence = new AtomicLong(1);   
    }

    @Override
    public Slot save(Slot slot) {
        if (slot.getId() == null) {
            try {
               // reflection 으로 id 주입
                Field idField = Slot.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(slot, sequence.getAndIncrement());
            } catch (Exception e) {
                throw new RuntimeException("[SlotMemoryRepository] 아이디 주입 실패", e);
            }
        }
        store.put(slot.getId(), slot);
        return slot;
    }

    @Override
    public Optional<Slot> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}

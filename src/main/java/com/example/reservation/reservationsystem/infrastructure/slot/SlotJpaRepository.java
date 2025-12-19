package com.example.reservation.reservationsystem.infrastructure.slot;

import com.example.reservation.reservationsystem.domain.slot.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SlotJpaRepository extends JpaRepository<Slot, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Slot s
           set s.remaining = s.remaining + 1
         where s.id = :slotId
           and s.remaining < s.capacity
    """)
    int increaseRemainingAtomic(@Param("slotId") Long slotId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Slot s
           set s.remaining = s.remaining - 1
         where s.id = :slotId
           and s.status = com.example.reservation.reservationsystem.domain.slot.SlotStatus.OPEN
           and s.remaining > 0
    """)
    int decreaseRemainingAtomic(@Param("slotId") Long slotId);
}

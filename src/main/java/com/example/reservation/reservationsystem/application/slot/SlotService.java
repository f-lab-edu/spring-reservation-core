package com.example.reservation.reservationsystem.application.slot;

import com.example.reservation.reservationsystem.application.slot.dto.SlotCreateRequest;
import com.example.reservation.reservationsystem.application.slot.dto.SlotResponse;
import com.example.reservation.reservationsystem.domain.slot.Slot;
import com.example.reservation.reservationsystem.domain.slot.SlotRepository;
import com.example.reservation.reservationsystem.domain.slot.SlotStatus;
import com.example.reservation.reservationsystem.domain.slot.exception.SlotErrorCode;
import com.example.reservation.reservationsystem.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlotService {

    private final SlotRepository slotRepository;

    @Transactional
    public Long createSlot(SlotCreateRequest request) {
        Slot slot = Slot.builder()
                .title(request.title())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .capacity(request.capacity())
                .remaining(request.capacity())
                .status(SlotStatus.OPEN)
                .build();

        Slot savedSlot = slotRepository.save(slot);
        return savedSlot.getId();
    }

    public SlotResponse getSlot(Long id) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SlotErrorCode.SLOT_NOT_FOUND));
        return SlotResponse.from(slot);
    }

    public Page<SlotResponse> getSlots(Pageable pageable) {
        return slotRepository.findAll(pageable)
                .map(SlotResponse::from);
    }
}

package com.example.reservation.reservationsystem.application.slot;

import com.example.reservation.reservationsystem.application.slot.dto.SlotCreateRequest;
import com.example.reservation.reservationsystem.application.slot.dto.SlotCreateResponse;
import com.example.reservation.reservationsystem.application.slot.dto.SlotResponse;
import com.example.reservation.reservationsystem.domain.slot.Slot;
import com.example.reservation.reservationsystem.domain.slot.exception.SlotErrorCode;
import com.example.reservation.reservationsystem.global.error.exception.BusinessException;
import com.example.reservation.reservationsystem.infrastructure.slot.SlotJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlotService {

    private final SlotJpaRepository slotRepository;

    @Transactional
    public SlotCreateResponse createSlot(SlotCreateRequest request) {
        Slot slot = Slot.of(request);

        return SlotCreateResponse.from(slotRepository.save(slot).getId());
    }

    public SlotResponse getSlot(Long id) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SlotErrorCode.SLOT_NOT_FOUND));
        return SlotResponse.from(slot);
    }

    @Transactional
    public void cancelSlotReservation(Long slotId) {
        int updated = slotRepository.increaseRemainingAtomic(slotId);
        if (updated == 0) {
            throw new BusinessException(SlotErrorCode.SLOT_NO_RESERVATION_TO_CANCEL);
        }
    }

    @Transactional
    public void reserveSlot(Long slotId) {
        int updated = slotRepository.decreaseRemainingAtomic(slotId);
        if (updated == 0) {
            throw new BusinessException(SlotErrorCode.SLOT_NOT_RESERVABLE);
        }
    }

    public Page<SlotResponse> getSlots(Pageable pageable) {
        Pageable pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                by(
                        desc("createdAt"),
                        desc("id")));

        return slotRepository.findAll(pageRequest)
                .map(SlotResponse::from);
    }
}

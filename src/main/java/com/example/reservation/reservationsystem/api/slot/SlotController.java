package com.example.reservation.reservationsystem.api.slot;

import com.example.reservation.reservationsystem.application.slot.SlotService;
import com.example.reservation.reservationsystem.application.slot.dto.SlotCreateRequest;
import com.example.reservation.reservationsystem.application.slot.dto.SlotCreateResponse;
import com.example.reservation.reservationsystem.application.slot.dto.SlotResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slots")
public class SlotController {

    private final SlotService slotService;

    @PostMapping("")
    public SlotCreateResponse createSlot(@Valid @RequestBody SlotCreateRequest request) {
        return slotService.createSlot(request);
    }

    @GetMapping("/{slotId}")
    public SlotResponse getSlot(@PathVariable Long slotId) {
        return slotService.getSlot(slotId);
    }

    @GetMapping
    public Page<SlotResponse> getSlots(@PageableDefault(size = 20) Pageable pageable) {
        return slotService.getSlots(pageable);
    }
}

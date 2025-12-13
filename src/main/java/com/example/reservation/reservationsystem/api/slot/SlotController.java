package com.example.reservation.reservationsystem.api.slot;

import com.example.reservation.reservationsystem.application.slot.SlotService;
import com.example.reservation.reservationsystem.application.slot.dto.SlotCreateRequest;
import com.example.reservation.reservationsystem.application.slot.dto.SlotResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slots")
public class SlotController {

    private final SlotService slotService;

    @PostMapping("")
    public ResponseEntity<Long> createSlot(@Valid @RequestBody SlotCreateRequest request, Locale locale) {
        Long slotId = slotService.createSlot(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(slotId)
                .toUri();
        return ResponseEntity.created(location).body(slotId);
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<SlotResponse> getSlot(@PathVariable Long slotId) {
        return ResponseEntity.ok(slotService.getSlot(slotId));
    }

    @GetMapping
    public ResponseEntity<Page<SlotResponse>> getSlots(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(slotService.getSlots(pageable));
    }
}

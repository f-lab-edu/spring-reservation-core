package com.example.reservation.reservationsystem.api.slot;

import com.example.reservation.reservationsystem.application.slot.SlotService;
import com.example.reservation.reservationsystem.application.slot.dto.SlotCreateRequest;
import com.example.reservation.reservationsystem.application.slot.dto.SlotResponse;
import com.example.reservation.reservationsystem.domain.slot.SlotStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SlotController.class)
class SlotControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private SlotService slotService;

        private final String TEST_NAME = "test slot";

        @Test
        @DisplayName("Slot 생성")
        void createSlot() throws Exception {
                // given
                SlotCreateRequest request = new SlotCreateRequest(
                                TEST_NAME,
                                LocalDateTime.now().plusDays(1),
                                LocalDateTime.now().plusDays(1).plusHours(1),
                                10);

                SlotResponse response = new SlotResponse(
                                1L,
                                TEST_NAME,
                                request.startAt(),
                                request.endAt(),
                                10,
                                10,
                                SlotStatus.OPEN);

                given(slotService.createSlot(any(SlotCreateRequest.class))).willReturn(1L);
                given(slotService.getSlot(1L)).willReturn(response);

                // when & then
                mockMvc.perform(post("/slots")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$").value(1L));
        }

        @Test
        @DisplayName("Slot 생성 실패")
        void createSlot_ValidationError() throws Exception {
                // given
                SlotCreateRequest request = new SlotCreateRequest(
                                "",
                                LocalDateTime.now().minusDays(1),
                                LocalDateTime.now().minusDays(1),
                                0
                );

                // when & then
                mockMvc.perform(post("/slots")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("G002"))
                                .andExpect(jsonPath("$.message").value("잘못된 입력 값입니다."))
                                .andExpect(jsonPath("$.errors").isArray())
                                .andExpect(jsonPath("$.errors[0].field").exists())
                                .andExpect(jsonPath("$.errors[0].value").exists())
                                .andExpect(jsonPath("$.errors[0].reason").exists());
        }

        @Test
        @DisplayName("Slot 단건 조회")
        void getSlot() throws Exception {
                // given
                SlotResponse response = new SlotResponse(
                                1L,
                                TEST_NAME,
                                LocalDateTime.now(),
                                LocalDateTime.now().plusHours(1),
                                10,
                                10,
                                SlotStatus.OPEN);
                given(slotService.getSlot(1L)).willReturn(response);

                // when & then
                mockMvc.perform(get("/slots/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.title").value(TEST_NAME));
        }

        @Test
        @DisplayName("Slot 리스트 조회 성공")
        void getSlots() throws Exception {
                // given
                List<SlotResponse> content = List.of(
                                new SlotResponse(1L, "Slot 1", LocalDateTime.now(), LocalDateTime.now(), 10, 10,
                                                SlotStatus.OPEN),
                                new SlotResponse(2L, "Slot 2", LocalDateTime.now(), LocalDateTime.now(), 10, 10,
                                                SlotStatus.OPEN));
                Page<SlotResponse> page = new PageImpl<>(content, PageRequest.of(0, 10), 2);

                given(slotService.getSlots(any(Pageable.class))).willReturn(page);

                // when & then
                mockMvc.perform(get("/slots")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(1L))
                                .andExpect(jsonPath("$.content[1].id").value(2L))
                                .andExpect(jsonPath("$.totalElements").value(2));
        }
}

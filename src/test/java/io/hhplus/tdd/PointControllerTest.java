package io.hhplus.tdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.controller.PointController;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PointService pointService;

    @RestControllerAdvice
    public static class GlobalExceptionHandler {
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    final long USER_ID = 1L;
    final long INIT_POINT = 10_000L;

    @Test
    @DisplayName("포인트_조회_성공")
    void getUserPoint_success() throws Exception {
        when(pointService.getUserPoint(USER_ID))
                .thenReturn(new UserPoint(USER_ID, INIT_POINT, System.currentTimeMillis()));

        mockMvc.perform(get("/point/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.point").value(10000));
    }

    @Test
    @DisplayName("포인트_충전_성공")
    void chargeUserPoint_success() throws Exception {
        long chargePoint = 5000L;
        long expectPoint = 15_000L;
        when(pointService.chargeUserPoint(USER_ID, chargePoint))
                .thenReturn(new UserPoint(USER_ID, expectPoint, System.currentTimeMillis()));

        mockMvc.perform(patch("/point/{id}/charge", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chargePoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(expectPoint));
    }

    @Test
    @DisplayName("포인트_사용_성공")
    void usePoint_success() throws Exception {
        long usePoint = 5000L;
        when(pointService.usePoint(USER_ID, usePoint))
                .thenReturn(new UserPoint(USER_ID, 5000L, System.currentTimeMillis()));

        mockMvc.perform(patch("/point/{id}/use", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usePoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(5000));
    }

}

package com.example.limit.controller;

import com.example.limit.dto.ConfirmOperationRequest;
import com.example.limit.dto.LimitRequest;
import com.example.limit.dto.LimitResponse;
import com.example.limit.dto.ReservationResponse;
import com.example.limit.exception.InsufficientLimitException;
import com.example.limit.exception.ReservationNotFoundException;
import com.example.limit.service.LimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LimitController.class)
class LimitControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private LimitService limitService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final Long USER_ID = 1L;
    private static final BigDecimal DEFAULT_LIMIT = new BigDecimal("100000.00");
    
    @Test
    void testGetLimit() throws Exception {
        // Given
        LimitResponse response = new LimitResponse(
                USER_ID,
                DEFAULT_LIMIT,
                DEFAULT_LIMIT,
                DEFAULT_LIMIT,
                LocalDateTime.now()
        );
        
        when(limitService.getLimit(USER_ID)).thenReturn(response);
        
        // When/Then
        mockMvc.perform(get("/api/limits/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.currentLimit").value(DEFAULT_LIMIT.doubleValue()))
                .andExpect(jsonPath("$.defaultLimit").value(DEFAULT_LIMIT.doubleValue()));
    }
    
    @Test
    void testReserveLimit() throws Exception {
        // Given
        LimitRequest request = new LimitRequest(new BigDecimal("1000.00"), "op-123");
        ReservationResponse response = new ReservationResponse();
        response.setReservationId(1L);
        response.setUserId(USER_ID);
        response.setAmount(request.getAmount());
        response.setOperationId("op-123");
        response.setStatus("PENDING");
        
        when(limitService.reserveLimit(eq(USER_ID), eq(request.getAmount()), eq("op-123")))
                .thenReturn(response);
        
        // When/Then
        mockMvc.perform(post("/api/limits/{userId}/reserve", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.operationId").value("op-123"))
                .andExpect(jsonPath("$.amount").value(1000.00));
    }
    
    @Test
    void testReserveLimit_InsufficientLimit() throws Exception {
        // Given
        LimitRequest request = new LimitRequest(new BigDecimal("200000.00"), "op-123");
        
        when(limitService.reserveLimit(eq(USER_ID), any(), any()))
                .thenThrow(new InsufficientLimitException("Insufficient limit"));
        
        // When/Then
        mockMvc.perform(post("/api/limits/{userId}/reserve", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testConfirmOperation() throws Exception {
        // Given
        ConfirmOperationRequest request = new ConfirmOperationRequest("op-123");
        doNothing().when(limitService).confirmOperation("op-123");
        
        // When/Then
        mockMvc.perform(post("/api/limits/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(limitService).confirmOperation("op-123");
    }
    
    @Test
    void testCancelOperation() throws Exception {
        // Given
        ConfirmOperationRequest request = new ConfirmOperationRequest("op-123");
        doNothing().when(limitService).cancelOperation("op-123");
        
        // When/Then
        mockMvc.perform(post("/api/limits/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(limitService).cancelOperation("op-123");
    }
    
    @Test
    void testRestoreLimit() throws Exception {
        // Given
        LimitRequest request = new LimitRequest(new BigDecimal("1000.00"), null);
        doNothing().when(limitService).restoreLimit(USER_ID, request.getAmount());
        
        // When/Then
        mockMvc.perform(post("/api/limits/{userId}/restore", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(limitService).restoreLimit(USER_ID, request.getAmount());
    }
    
    @Test
    void testDeductLimit() throws Exception {
        // Given
        LimitRequest request = new LimitRequest(new BigDecimal("1000.00"), null);
        doNothing().when(limitService).deductLimit(USER_ID, request.getAmount());
        
        // When/Then
        mockMvc.perform(post("/api/limits/{userId}/deduct", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(limitService).deductLimit(USER_ID, request.getAmount());
    }
    
    @Test
    void testConfirmOperation_ReservationNotFound() throws Exception {
        // Given
        ConfirmOperationRequest request = new ConfirmOperationRequest("op-123");
        doThrow(new ReservationNotFoundException("Reservation not found"))
                .when(limitService).confirmOperation("op-123");
        
        // When/Then
        mockMvc.perform(post("/api/limits/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}



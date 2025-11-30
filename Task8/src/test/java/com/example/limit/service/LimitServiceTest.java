package com.example.limit.service;

import com.example.limit.dto.LimitResponse;
import com.example.limit.dto.ReservationResponse;
import com.example.limit.entity.LimitReservation;
import com.example.limit.entity.UserLimit;
import com.example.limit.exception.InsufficientLimitException;
import com.example.limit.exception.ReservationNotFoundException;
import com.example.limit.repository.LimitReservationRepository;
import com.example.limit.repository.UserLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimitServiceTest {
    
    @Mock
    private UserLimitRepository userLimitRepository;
    
    @Mock
    private LimitReservationRepository reservationRepository;
    
    @InjectMocks
    private LimitService limitService;
    
    private static final BigDecimal DEFAULT_LIMIT = new BigDecimal("100000.00");
    private static final Long USER_ID = 1L;
    
    @BeforeEach
    void setUp() {
        // Создаем новый экземпляр сервиса с дефолтным лимитом
        limitService = new LimitService(userLimitRepository, reservationRepository, DEFAULT_LIMIT);
    }
    
    @Test
    void testGetLimit_ExistingUser() {
        // Given
        UserLimit userLimit = new UserLimit(USER_ID, DEFAULT_LIMIT, DEFAULT_LIMIT);
        userLimit.setUpdatedAt(LocalDateTime.now());
        
        when(userLimitRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userLimit));
        when(reservationRepository.sumReservedAmountByUserIdAndStatus(USER_ID, LimitReservation.ReservationStatus.PENDING))
                .thenReturn(BigDecimal.ZERO);
        
        // When
        LimitResponse response = limitService.getLimit(USER_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(USER_ID, response.getUserId());
        assertEquals(DEFAULT_LIMIT, response.getCurrentLimit());
        assertEquals(DEFAULT_LIMIT, response.getDefaultLimit());
        assertEquals(DEFAULT_LIMIT, response.getAvailableLimit());
    }
    
    @Test
    void testGetLimit_NewUser() {
        // Given
        UserLimit newUserLimit = new UserLimit(USER_ID, DEFAULT_LIMIT, DEFAULT_LIMIT);
        newUserLimit.setUpdatedAt(LocalDateTime.now());
        
        when(userLimitRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userLimitRepository.save(any(UserLimit.class))).thenReturn(newUserLimit);
        when(reservationRepository.sumReservedAmountByUserIdAndStatus(USER_ID, LimitReservation.ReservationStatus.PENDING))
                .thenReturn(BigDecimal.ZERO);
        
        // When
        LimitResponse response = limitService.getLimit(USER_ID);
        
        // Then
        assertNotNull(response);
        verify(userLimitRepository).save(any(UserLimit.class));
    }
    
    @Test
    void testReserveLimit_Success() {
        // Given
        BigDecimal amount = new BigDecimal("1000.00");
        String operationId = "op-123";
        UserLimit userLimit = new UserLimit(USER_ID, DEFAULT_LIMIT, DEFAULT_LIMIT);
        LimitReservation reservation = new LimitReservation(USER_ID, amount, operationId);
        reservation.setId(1L);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setExpiresAt(LocalDateTime.now().plusHours(1));
        
        when(userLimitRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userLimit));
        when(reservationRepository.findByOperationId(operationId)).thenReturn(Optional.empty());
        when(reservationRepository.sumReservedAmountByUserIdAndStatus(USER_ID, LimitReservation.ReservationStatus.PENDING))
                .thenReturn(BigDecimal.ZERO);
        when(reservationRepository.save(any(LimitReservation.class))).thenReturn(reservation);
        
        // When
        ReservationResponse response = limitService.reserveLimit(USER_ID, amount, operationId);
        
        // Then
        assertNotNull(response);
        assertEquals(operationId, response.getOperationId());
        assertEquals(amount, response.getAmount());
        verify(reservationRepository).save(any(LimitReservation.class));
    }
    
    @Test
    void testReserveLimit_InsufficientLimit() {
        // Given
        BigDecimal amount = new BigDecimal("200000.00");
        UserLimit userLimit = new UserLimit(USER_ID, DEFAULT_LIMIT, DEFAULT_LIMIT);
        
        when(userLimitRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userLimit));
        when(reservationRepository.findByOperationId(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.sumReservedAmountByUserIdAndStatus(USER_ID, LimitReservation.ReservationStatus.PENDING))
                .thenReturn(BigDecimal.ZERO);
        
        // When/Then
        assertThrows(InsufficientLimitException.class, () -> 
                limitService.reserveLimit(USER_ID, amount, null));
    }
    
    @Test
    void testConfirmOperation_Success() {
        // Given
        String operationId = "op-123";
        BigDecimal amount = new BigDecimal("1000.00");
        LimitReservation reservation = new LimitReservation(USER_ID, amount, operationId);
        reservation.setStatus(LimitReservation.ReservationStatus.PENDING);
        
        UserLimit userLimit = new UserLimit(USER_ID, DEFAULT_LIMIT, DEFAULT_LIMIT);
        
        when(reservationRepository.findByOperationId(operationId)).thenReturn(Optional.of(reservation));
        when(userLimitRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userLimit));
        when(reservationRepository.sumReservedAmountByUserIdAndStatus(USER_ID, LimitReservation.ReservationStatus.PENDING))
                .thenReturn(amount);
        
        // When
        limitService.confirmOperation(operationId);
        
        // Then
        assertEquals(LimitReservation.ReservationStatus.CONFIRMED, reservation.getStatus());
        verify(userLimitRepository).save(userLimit);
        verify(reservationRepository).save(reservation);
    }
    
    @Test
    void testConfirmOperation_ReservationNotFound() {
        // Given
        String operationId = "op-123";
        when(reservationRepository.findByOperationId(operationId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThrows(ReservationNotFoundException.class, () -> 
                limitService.confirmOperation(operationId));
    }
    
    @Test
    void testCancelOperation_Pending() {
        // Given
        String operationId = "op-123";
        BigDecimal amount = new BigDecimal("1000.00");
        LimitReservation reservation = new LimitReservation(USER_ID, amount, operationId);
        reservation.setStatus(LimitReservation.ReservationStatus.PENDING);
        
        when(reservationRepository.findByOperationId(operationId)).thenReturn(Optional.of(reservation));
        
        // When
        limitService.cancelOperation(operationId);
        
        // Then
        assertEquals(LimitReservation.ReservationStatus.CANCELLED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
        verify(userLimitRepository, never()).save(any());
    }
    
    @Test
    void testCancelOperation_Confirmed() {
        // Given
        String operationId = "op-123";
        BigDecimal amount = new BigDecimal("1000.00");
        LimitReservation reservation = new LimitReservation(USER_ID, amount, operationId);
        reservation.setStatus(LimitReservation.ReservationStatus.CONFIRMED);
        
        UserLimit userLimit = new UserLimit(USER_ID, new BigDecimal("99000.00"), DEFAULT_LIMIT);
        
        when(reservationRepository.findByOperationId(operationId)).thenReturn(Optional.of(reservation));
        when(userLimitRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userLimit));
        
        // When
        limitService.cancelOperation(operationId);
        
        // Then
        assertEquals(LimitReservation.ReservationStatus.CANCELLED, reservation.getStatus());
        verify(userLimitRepository).save(userLimit);
        assertEquals(new BigDecimal("100000.00"), userLimit.getCurrentLimit());
    }
    
    @Test
    void testRestoreLimit() {
        // Given
        BigDecimal amount = new BigDecimal("1000.00");
        UserLimit userLimit = new UserLimit(USER_ID, new BigDecimal("99000.00"), DEFAULT_LIMIT);
        
        when(userLimitRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userLimit));
        
        // When
        limitService.restoreLimit(USER_ID, amount);
        
        // Then
        assertEquals(new BigDecimal("100000.00"), userLimit.getCurrentLimit());
        verify(userLimitRepository).save(userLimit);
    }
    
    @Test
    void testDeductLimit_Success() {
        // Given
        BigDecimal amount = new BigDecimal("1000.00");
        UserLimit userLimit = new UserLimit(USER_ID, DEFAULT_LIMIT, DEFAULT_LIMIT);
        
        when(userLimitRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userLimit));
        when(reservationRepository.sumReservedAmountByUserIdAndStatus(USER_ID, LimitReservation.ReservationStatus.PENDING))
                .thenReturn(BigDecimal.ZERO);
        
        // When
        limitService.deductLimit(USER_ID, amount);
        
        // Then
        assertEquals(new BigDecimal("99000.00"), userLimit.getCurrentLimit());
        verify(userLimitRepository).save(userLimit);
    }
    
    @Test
    void testDeductLimit_InsufficientLimit() {
        // Given
        BigDecimal amount = new BigDecimal("200000.00");
        UserLimit userLimit = new UserLimit(USER_ID, DEFAULT_LIMIT, DEFAULT_LIMIT);
        
        when(userLimitRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userLimit));
        when(reservationRepository.sumReservedAmountByUserIdAndStatus(USER_ID, LimitReservation.ReservationStatus.PENDING))
                .thenReturn(BigDecimal.ZERO);
        
        // When/Then
        assertThrows(InsufficientLimitException.class, () -> 
                limitService.deductLimit(USER_ID, amount));
    }
}


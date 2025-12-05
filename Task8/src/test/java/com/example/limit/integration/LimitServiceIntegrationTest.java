package com.example.limit.integration;

import com.example.limit.dto.LimitResponse;
import com.example.limit.dto.ReservationResponse;
import com.example.limit.entity.LimitReservation;
import com.example.limit.entity.UserLimit;
import com.example.limit.exception.InsufficientLimitException;
import com.example.limit.exception.ReservationNotFoundException;
import com.example.limit.repository.LimitReservationRepository;
import com.example.limit.repository.UserLimitRepository;
import com.example.limit.service.LimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "limit.default-value=100000.00",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false"
})
@ActiveProfiles("test")
class LimitServiceIntegrationTest {
    
    @Autowired
    private LimitService limitService;
    
    @Autowired
    private UserLimitRepository userLimitRepository;
    
    @Autowired
    private LimitReservationRepository reservationRepository;
    
    private static final Long USER_ID = 1L;
    private static final Long NEW_USER_ID = 999L;
    
    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        userLimitRepository.deleteAll();
    }
    
    @Test
    void testGetLimit_NewUser_CreatesDefaultLimit() {
        // When
        LimitResponse response = limitService.getLimit(NEW_USER_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(NEW_USER_ID, response.getUserId());
        assertEquals(new BigDecimal("100000.00"), response.getCurrentLimit());
        assertEquals(new BigDecimal("100000.00"), response.getDefaultLimit());
        
        Optional<UserLimit> savedLimit = userLimitRepository.findByUserId(NEW_USER_ID);
        assertTrue(savedLimit.isPresent());
    }
    
    @Test
    void testReserveLimit_AndConfirm_FullFlow() {
        // Given
        BigDecimal amount = new BigDecimal("5000.00");
        String operationId = "op-123";
        
        // When - резервируем
        ReservationResponse reservation = limitService.reserveLimit(USER_ID, amount, operationId);
        
        // Then
        assertNotNull(reservation);
        assertEquals(operationId, reservation.getOperationId());
        assertEquals(amount, reservation.getAmount());
        assertEquals("PENDING", reservation.getStatus());
        
        // Проверяем, что лимит не списан, но зарезервирован
        LimitResponse limitAfterReserve = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("100000.00"), limitAfterReserve.getCurrentLimit());
        assertEquals(new BigDecimal("95000.00"), limitAfterReserve.getAvailableLimit());
        
        // When - подтверждаем операцию
        limitService.confirmOperation(operationId);
        
        // Then - лимит должен быть списан
        LimitResponse limitAfterConfirm = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("95000.00"), limitAfterConfirm.getCurrentLimit());
        assertEquals(new BigDecimal("95000.00"), limitAfterConfirm.getAvailableLimit());
        
        Optional<LimitReservation> confirmedReservation = reservationRepository.findByOperationId(operationId);
        assertTrue(confirmedReservation.isPresent());
        assertEquals(LimitReservation.ReservationStatus.CONFIRMED, confirmedReservation.get().getStatus());
    }
    
    @Test
    void testReserveLimit_AndCancel_FreesReservation() {
        // Given
        BigDecimal amount = new BigDecimal("5000.00");
        String operationId = "op-456";
        
        // When - резервируем
        limitService.reserveLimit(USER_ID, amount, operationId);
        
        // Then
        LimitResponse limitAfterReserve = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("95000.00"), limitAfterReserve.getAvailableLimit());
        
        // When - отменяем операцию
        limitService.cancelOperation(operationId);
        
        // Then - резерв должен быть освобожден
        LimitResponse limitAfterCancel = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("100000.00"), limitAfterCancel.getAvailableLimit());
        
        Optional<LimitReservation> cancelledReservation = reservationRepository.findByOperationId(operationId);
        assertTrue(cancelledReservation.isPresent());
        assertEquals(LimitReservation.ReservationStatus.CANCELLED, cancelledReservation.get().getStatus());
    }
    
    @Test
    void testReserveLimit_InsufficientLimit() {
        // Given
        BigDecimal amount = new BigDecimal("200000.00");
        
        // When/Then
        assertThrows(InsufficientLimitException.class, () -> 
                limitService.reserveLimit(USER_ID, amount, "op-789"));
    }
    
    @Test
    void testRestoreLimit_AfterDeduction() {
        // Given
        BigDecimal amount = new BigDecimal("10000.00");
        limitService.deductLimit(USER_ID, amount);
        
        LimitResponse limitAfterDeduct = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("90000.00"), limitAfterDeduct.getCurrentLimit());
        
        // When - восстанавливаем
        limitService.restoreLimit(USER_ID, amount);
        
        // Then
        LimitResponse limitAfterRestore = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("100000.00"), limitAfterRestore.getCurrentLimit());
    }
    
    @Test
    void testCancelOperation_AfterConfirm_RestoresLimit() {
        // Given
        BigDecimal amount = new BigDecimal("5000.00");
        String operationId = "op-confirm-cancel";
        
        // Резервируем и подтверждаем
        limitService.reserveLimit(USER_ID, amount, operationId);
        limitService.confirmOperation(operationId);
        
        LimitResponse limitAfterConfirm = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("95000.00"), limitAfterConfirm.getCurrentLimit());
        
        // When - отменяем уже подтвержденную операцию
        limitService.cancelOperation(operationId);
        
        // Then - лимит должен быть восстановлен
        LimitResponse limitAfterCancel = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("100000.00"), limitAfterCancel.getCurrentLimit());
    }
    
    @Test
    void testMultipleReservations() {
        // Given
        BigDecimal amount1 = new BigDecimal("30000.00");
        BigDecimal amount2 = new BigDecimal("40000.00");
        BigDecimal amount3 = new BigDecimal("20000.00");
        
        // When - создаем несколько резервов
        limitService.reserveLimit(USER_ID, amount1, "op-1");
        limitService.reserveLimit(USER_ID, amount2, "op-2");
        limitService.reserveLimit(USER_ID, amount3, "op-3");
        
        // Then - доступный лимит должен учитывать все резервы
        LimitResponse limit = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("100000.00"), limit.getCurrentLimit());
        assertEquals(new BigDecimal("10000.00"), limit.getAvailableLimit()); // 100000 - 30000 - 40000 - 20000
        
        // When - подтверждаем одну операцию
        limitService.confirmOperation("op-1");
        
        // Then
        LimitResponse limitAfterConfirm = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("70000.00"), limitAfterConfirm.getCurrentLimit());
        assertEquals(new BigDecimal("10000.00"), limitAfterConfirm.getAvailableLimit()); // 70000 - 40000 - 20000
    }
    
    @Test
    void testResetAllLimits() {
        // Given
        limitService.deductLimit(USER_ID, new BigDecimal("50000.00"));
        limitService.reserveLimit(USER_ID, new BigDecimal("20000.00"), "op-reset");
        
        LimitResponse limitBeforeReset = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("50000.00"), limitBeforeReset.getCurrentLimit());
        
        // When
        limitService.resetAllLimits();
        
        // Then
        LimitResponse limitAfterReset = limitService.getLimit(USER_ID);
        assertEquals(new BigDecimal("100000.00"), limitAfterReset.getCurrentLimit());
        assertEquals(new BigDecimal("100000.00"), limitAfterReset.getAvailableLimit());
        
        Optional<LimitReservation> reservation = reservationRepository.findByOperationId("op-reset");
        assertTrue(reservation.isPresent());
        assertEquals(LimitReservation.ReservationStatus.CANCELLED, reservation.get().getStatus());
    }
    
    @Test
    void testConfirmOperation_ReservationNotFound() {
        // When/Then
        assertThrows(ReservationNotFoundException.class, () -> 
                limitService.confirmOperation("non-existent-op"));
    }
}


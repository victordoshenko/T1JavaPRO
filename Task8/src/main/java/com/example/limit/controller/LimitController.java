package com.example.limit.controller;

import com.example.limit.dto.ConfirmOperationRequest;
import com.example.limit.dto.LimitRequest;
import com.example.limit.dto.LimitResponse;
import com.example.limit.dto.ReservationResponse;
import com.example.limit.exception.InsufficientLimitException;
import com.example.limit.exception.ReservationNotFoundException;
import com.example.limit.service.LimitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/limits")
public class LimitController {
    
    private final LimitService limitService;
    
    public LimitController(LimitService limitService) {
        this.limitService = limitService;
    }
    
    /**
     * GET /api/limits/{userId} - Получить информацию о лимите пользователя
     */
    @GetMapping("/{userId}")
    public ResponseEntity<LimitResponse> getLimit(@PathVariable Long userId) {
        LimitResponse response = limitService.getLimit(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/limits/{userId}/reserve - Зарезервировать лимит
     */
    @PostMapping("/{userId}/reserve")
    public ResponseEntity<ReservationResponse> reserveLimit(
            @PathVariable Long userId,
            @Valid @RequestBody LimitRequest request) {
        ReservationResponse response = limitService.reserveLimit(
                userId,
                request.getAmount(),
                request.getOperationId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * POST /api/limits/confirm - Подтвердить операцию (списать лимит)
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmOperation(@Valid @RequestBody ConfirmOperationRequest request) {
        limitService.confirmOperation(request.getOperationId());
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/limits/cancel - Отменить операцию (освободить резерв)
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelOperation(@Valid @RequestBody ConfirmOperationRequest request) {
        limitService.cancelOperation(request.getOperationId());
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/limits/{userId}/restore - Восстановить лимит
     */
    @PostMapping("/{userId}/restore")
    public ResponseEntity<Void> restoreLimit(
            @PathVariable Long userId,
            @Valid @RequestBody LimitRequest request) {
        limitService.restoreLimit(userId, request.getAmount());
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/limits/{userId}/deduct - Прямое списание лимита (без резервирования)
     */
    @PostMapping("/{userId}/deduct")
    public ResponseEntity<Void> deductLimit(
            @PathVariable Long userId,
            @Valid @RequestBody LimitRequest request) {
        limitService.deductLimit(userId, request.getAmount());
        return ResponseEntity.ok().build();
    }
    
    @ExceptionHandler(InsufficientLimitException.class)
    public ResponseEntity<String> handleInsufficientLimit(InsufficientLimitException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
    
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<String> handleReservationNotFound(ReservationNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}



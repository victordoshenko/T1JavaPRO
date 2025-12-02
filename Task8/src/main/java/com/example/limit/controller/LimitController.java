package com.example.limit.controller;

import com.example.limit.dto.ConfirmOperationRequest;
import com.example.limit.dto.LimitErrorResponse;
import com.example.limit.dto.LimitRequest;
import com.example.limit.dto.LimitResponse;
import com.example.limit.dto.ReservationResponse;
import com.example.limit.exception.InsufficientLimitException;
import com.example.limit.exception.ReservationNotFoundException;
import com.example.limit.service.LimitServiceApi;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/limits")
public class LimitController {
    
    private final LimitServiceApi limitService;
    
    public LimitController(LimitServiceApi limitService) {
        this.limitService = limitService;
    }
    
    /**
     * GET /api/limits/{userId} - Получить информацию о лимите пользователя
     */
    @GetMapping("/{userId}")
    public LimitResponse getLimit(@PathVariable Long userId) {
        return limitService.getLimit(userId);
    }
    
    /**
     * POST /api/limits/{userId}/reserve - Зарезервировать лимит
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/reserve")
    public ReservationResponse reserveLimit(
            @PathVariable Long userId,
            @Valid @RequestBody LimitRequest request) {
        return limitService.reserveLimit(
                userId,
                request.getAmount(),
                request.getOperationId()
        );
    }
    
    /**
     * POST /api/limits/confirm - Подтвердить операцию (списать лимит)
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/confirm")
    public void confirmOperation(@Valid @RequestBody ConfirmOperationRequest request) {
        limitService.confirmOperation(request.getOperationId());
    }
    
    /**
     * POST /api/limits/cancel - Отменить операцию (освободить резерв)
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/cancel")
    public void cancelOperation(@Valid @RequestBody ConfirmOperationRequest request) {
        limitService.cancelOperation(request.getOperationId());
    }
    
    /**
     * POST /api/limits/{userId}/restore - Восстановить лимит
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/restore")
    public void restoreLimit(
            @PathVariable Long userId,
            @Valid @RequestBody LimitRequest request) {
        limitService.restoreLimit(userId, request.getAmount());
    }
    
    /**
     * POST /api/limits/{userId}/deduct - Прямое списание лимита (без резервирования)
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/deduct")
    public void deductLimit(
            @PathVariable Long userId,
            @Valid @RequestBody LimitRequest request) {
        limitService.deductLimit(userId, request.getAmount());
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InsufficientLimitException.class)
    public LimitErrorResponse handleInsufficientLimit(InsufficientLimitException e) {
        return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ReservationNotFoundException.class)
    public LimitErrorResponse handleReservationNotFound(ReservationNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public LimitErrorResponse handleIllegalArgument(IllegalArgumentException e) {
        return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalStateException.class)
    public LimitErrorResponse handleIllegalState(IllegalStateException e) {
        return errorResponse(HttpStatus.CONFLICT, e.getMessage());
    }
    
    private LimitErrorResponse errorResponse(HttpStatus status, String message) {
        return LimitErrorResponse.of(status.value(), status.getReasonPhrase(), message);
    }
}




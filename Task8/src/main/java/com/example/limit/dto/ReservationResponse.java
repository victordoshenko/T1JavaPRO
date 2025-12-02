package com.example.limit.dto;

import com.example.limit.entity.LimitReservation;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationResponse {
    
    private Long reservationId;
    private Long userId;
    private BigDecimal amount;
    private String status;
    private String operationId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    public ReservationResponse() {
    }
    
    public ReservationResponse(LimitReservation reservation) {
        this.reservationId = reservation.getId();
        this.userId = reservation.getUserId();
        this.amount = reservation.getAmount();
        this.status = reservation.getStatus().name();
        this.operationId = reservation.getOperationId();
        this.createdAt = reservation.getCreatedAt();
        this.expiresAt = reservation.getExpiresAt();
    }
    
    public Long getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getOperationId() {
        return operationId;
    }
    
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}




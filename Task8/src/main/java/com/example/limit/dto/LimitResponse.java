package com.example.limit.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LimitResponse {
    
    private Long userId;
    private BigDecimal currentLimit;
    private BigDecimal defaultLimit;
    private BigDecimal availableLimit;
    private LocalDateTime updatedAt;
    
    public LimitResponse() {
    }
    
    public LimitResponse(Long userId, BigDecimal currentLimit, BigDecimal defaultLimit, 
                        BigDecimal availableLimit, LocalDateTime updatedAt) {
        this.userId = userId;
        this.currentLimit = currentLimit;
        this.defaultLimit = defaultLimit;
        this.availableLimit = availableLimit;
        this.updatedAt = updatedAt;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getCurrentLimit() {
        return currentLimit;
    }
    
    public void setCurrentLimit(BigDecimal currentLimit) {
        this.currentLimit = currentLimit;
    }
    
    public BigDecimal getDefaultLimit() {
        return defaultLimit;
    }
    
    public void setDefaultLimit(BigDecimal defaultLimit) {
        this.defaultLimit = defaultLimit;
    }
    
    public BigDecimal getAvailableLimit() {
        return availableLimit;
    }
    
    public void setAvailableLimit(BigDecimal availableLimit) {
        this.availableLimit = availableLimit;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}




package com.example.limit.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_limits", uniqueConstraints = {
    @UniqueConstraint(columnNames = "user_id")
})
public class UserLimit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "current_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentLimit;
    
    @Column(name = "default_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal defaultLimit;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public UserLimit() {
    }
    
    public UserLimit(Long userId, BigDecimal currentLimit, BigDecimal defaultLimit) {
        this.userId = userId;
        this.currentLimit = currentLimit;
        this.defaultLimit = defaultLimit;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



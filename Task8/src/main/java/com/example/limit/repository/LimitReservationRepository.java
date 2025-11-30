package com.example.limit.repository;

import com.example.limit.entity.LimitReservation;
import com.example.limit.entity.LimitReservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LimitReservationRepository extends JpaRepository<LimitReservation, Long> {
    
    Optional<LimitReservation> findByOperationId(String operationId);
    
    List<LimitReservation> findByUserIdAndStatus(Long userId, ReservationStatus status);
    
    @Query("SELECT COALESCE(SUM(lr.amount), 0) FROM LimitReservation lr WHERE lr.userId = :userId AND lr.status = :status")
    java.math.BigDecimal sumReservedAmountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ReservationStatus status);
    
    @Modifying
    @Query("UPDATE LimitReservation lr SET lr.status = :newStatus WHERE lr.status = :oldStatus AND lr.expiresAt < :now")
    int expireOldReservations(@Param("oldStatus") ReservationStatus oldStatus, 
                              @Param("newStatus") ReservationStatus newStatus, 
                              @Param("now") LocalDateTime now);
}


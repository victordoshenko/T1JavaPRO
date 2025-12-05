package com.example.limit.service;

import com.example.limit.dto.LimitResponse;
import com.example.limit.dto.ReservationResponse;
import com.example.limit.entity.LimitReservation;
import com.example.limit.entity.UserLimit;
import com.example.limit.exception.InsufficientLimitException;
import com.example.limit.exception.ReservationNotFoundException;
import com.example.limit.repository.LimitReservationRepository;
import com.example.limit.repository.UserLimitRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LimitService implements LimitServiceApi {
    
    private final UserLimitRepository userLimitRepository;
    private final LimitReservationRepository reservationRepository;
    private final BigDecimal defaultLimitValue;
    
    public LimitService(
            UserLimitRepository userLimitRepository,
            LimitReservationRepository reservationRepository,
            @Value("${limit.default-value:100000.00}") BigDecimal defaultLimitValue) {
        this.userLimitRepository = userLimitRepository;
        this.reservationRepository = reservationRepository;
        this.defaultLimitValue = defaultLimitValue;
    }
    
    /**
     * Получить или создать лимит для пользователя
     */
    @Transactional
    public LimitResponse getLimit(Long userId) {
        UserLimit userLimit = userLimitRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultLimit(userId));
        
        BigDecimal reservedAmount = reservationRepository
                .sumReservedAmountByUserIdAndStatus(userId, LimitReservation.ReservationStatus.PENDING);
        
        BigDecimal availableLimit = userLimit.getCurrentLimit().subtract(reservedAmount);
        
        return new LimitResponse(
                userLimit.getUserId(),
                userLimit.getCurrentLimit(),
                userLimit.getDefaultLimit(),
                availableLimit,
                userLimit.getUpdatedAt()
        );
    }
    
    /**
     * Резервирование лимита на время операции
     */
    @Transactional
    public ReservationResponse reserveLimit(Long userId, BigDecimal amount, String operationId) {
        // Получаем или создаем лимит
        UserLimit userLimit = userLimitRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultLimit(userId));
        
        // Проверяем, не существует ли уже резерв с таким operationId
        final String finalOperationId;
        if (operationId != null && !operationId.isEmpty()) {
            reservationRepository.findByOperationId(operationId)
                    .ifPresent(r -> {
                        throw new IllegalArgumentException("Reservation with operationId " + operationId + " already exists");
                    });
            finalOperationId = operationId;
        } else {
            finalOperationId = UUID.randomUUID().toString();
        }
        
        // Вычисляем доступный лимит (текущий минус все pending резервы)
        BigDecimal reservedAmount = reservationRepository
                .sumReservedAmountByUserIdAndStatus(userId, LimitReservation.ReservationStatus.PENDING);
        
        BigDecimal availableLimit = userLimit.getCurrentLimit().subtract(reservedAmount);
        
        if (availableLimit.compareTo(amount) < 0) {
            throw new InsufficientLimitException(
                    String.format("Insufficient limit. Available: %s, Requested: %s", availableLimit, amount)
            );
        }
        
        // Создаем резерв
        LimitReservation reservation = new LimitReservation(userId, amount, finalOperationId);
        reservation.setExpiresAt(LocalDateTime.now().plusHours(1)); // Резерв действителен 1 час
        reservation = reservationRepository.save(reservation);
        
        return new ReservationResponse(reservation);
    }
    
    /**
     * Подтверждение операции - списание лимита
     */
    @Transactional
    public void confirmOperation(String operationId) {
        LimitReservation reservation = reservationRepository.findByOperationId(operationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found for operationId: " + operationId));
        
        if (reservation.getStatus() != LimitReservation.ReservationStatus.PENDING) {
            throw new IllegalStateException("Reservation is not in PENDING status");
        }
        
        // Получаем лимит пользователя
        UserLimit userLimit = userLimitRepository.findByUserId(reservation.getUserId())
                .orElseThrow(() -> new IllegalStateException("User limit not found"));
        
        // Списываем лимит. На этапе резервирования уже была проверка доступного лимита,
        // поэтому при подтверждении повторная проверка не требуется.
        userLimit.setCurrentLimit(userLimit.getCurrentLimit().subtract(reservation.getAmount()));
        userLimitRepository.save(userLimit);
        
        // Обновляем статус резерва
        reservation.setStatus(LimitReservation.ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
    }
    
    /**
     * Отмена операции - освобождение резерва
     */
    @Transactional
    public void cancelOperation(String operationId) {
        LimitReservation reservation = reservationRepository.findByOperationId(operationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found for operationId: " + operationId));
        
        if (reservation.getStatus() == LimitReservation.ReservationStatus.CONFIRMED) {
            // Если операция уже подтверждена, нужно восстановить лимит
            restoreLimit(reservation.getUserId(), reservation.getAmount());
        }
        
        reservation.setStatus(LimitReservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }
    
    /**
     * Восстановление лимита после неуспешной операции
     */
    @Transactional
    public void restoreLimit(Long userId, BigDecimal amount) {
        UserLimit userLimit = userLimitRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User limit not found"));
        
        userLimit.setCurrentLimit(userLimit.getCurrentLimit().add(amount));
        userLimitRepository.save(userLimit);
    }
    
    /**
     * Прямое списание лимита (без резервирования)
     */
    @Transactional
    public void deductLimit(Long userId, BigDecimal amount) {
        UserLimit userLimit = userLimitRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultLimit(userId));
        
        BigDecimal reservedAmount = reservationRepository
                .sumReservedAmountByUserIdAndStatus(userId, LimitReservation.ReservationStatus.PENDING);
        
        BigDecimal availableLimit = userLimit.getCurrentLimit().subtract(reservedAmount);
        
        if (availableLimit.compareTo(amount) < 0) {
            throw new InsufficientLimitException(
                    String.format("Insufficient limit. Available: %s, Requested: %s", availableLimit, amount)
            );
        }
        
        userLimit.setCurrentLimit(userLimit.getCurrentLimit().subtract(amount));
        userLimitRepository.save(userLimit);
    }
    
    /**
     * Сброс всех лимитов до дефолтного значения
     */
    @Transactional
    public void resetAllLimits() {
        userLimitRepository.resetAllLimitsToDefault(defaultLimitValue);
        
        // Отменяем все pending резервы одним bulk-запросом
        reservationRepository.updateStatusForAllByCurrentStatus(
                LimitReservation.ReservationStatus.PENDING,
                LimitReservation.ReservationStatus.CANCELLED
        );
    }
    
    /**
     * Очистка истекших резервов
     */
    @Transactional
    public void expireOldReservations() {
        LocalDateTime now = LocalDateTime.now();
        reservationRepository.expireOldReservations(
                LimitReservation.ReservationStatus.PENDING,
                LimitReservation.ReservationStatus.EXPIRED,
                now
        );
    }
    
    /**
     * Обновление дефолтного значения лимита для всех пользователей
     */
    @Transactional
    public void updateDefaultLimit(BigDecimal newDefaultLimit) {
        userLimitRepository.updateDefaultLimitForAll(newDefaultLimit);
    }
    
    /**
     * Создание дефолтного лимита для пользователя
     */
    private UserLimit createDefaultLimit(Long userId) {
        UserLimit userLimit = new UserLimit(userId, defaultLimitValue, defaultLimitValue);
        return userLimitRepository.save(userLimit);
    }
}


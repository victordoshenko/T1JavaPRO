package com.example.limit.service;

import com.example.limit.dto.LimitResponse;
import com.example.limit.dto.ReservationResponse;

import java.math.BigDecimal;

/**
 * Публичный контракт сервиса лимитов.
 * Используется контроллером и в тестах, чтобы мокать интерфейс,
 * а не конкретный класс, что лучше работает с новыми версиями JDK.
 */
public interface LimitServiceApi {

    LimitResponse getLimit(Long userId);

    ReservationResponse reserveLimit(Long userId, BigDecimal amount, String operationId);

    void confirmOperation(String operationId);

    void cancelOperation(String operationId);

    void restoreLimit(Long userId, BigDecimal amount);

    void deductLimit(Long userId, BigDecimal amount);
}



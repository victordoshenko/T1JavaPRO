package com.example.limit.dto;

import java.time.Instant;

/**
 * Унифицированный ответ об ошибке для лимитного модуля.
 */
public record LimitErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message
) {
    public static LimitErrorResponse of(int status, String error, String message) {
        return new LimitErrorResponse(Instant.now(), status, error, message);
    }
}


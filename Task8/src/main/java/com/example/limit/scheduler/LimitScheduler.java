package com.example.limit.scheduler;

import com.example.limit.service.LimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LimitScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(LimitScheduler.class);
    
    private final LimitService limitService;
    
    public LimitScheduler(LimitService limitService) {
        this.limitService = limitService;
    }
    
    /**
     * Сброс лимитов каждый день в 00:00
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetLimitsDaily() {
        logger.info("Starting daily limit reset at midnight");
        try {
            limitService.resetAllLimits();
            logger.info("Daily limit reset completed successfully");
        } catch (Exception e) {
            logger.error("Error during daily limit reset", e);
        }
    }
    
    /**
     * Очистка истекших резервов каждый час
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void expireOldReservations() {
        logger.debug("Cleaning up expired reservations");
        try {
            limitService.expireOldReservations();
        } catch (Exception e) {
            logger.error("Error during expired reservations cleanup", e);
        }
    }
}



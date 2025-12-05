package com.example.limit.repository;

import com.example.limit.entity.UserLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface UserLimitRepository extends JpaRepository<UserLimit, Long> {
    
    Optional<UserLimit> findByUserId(Long userId);
    
    @Modifying
    @Query("UPDATE UserLimit ul SET ul.currentLimit = :defaultLimit, ul.updatedAt = CURRENT_TIMESTAMP")
    int resetAllLimitsToDefault(@Param("defaultLimit") BigDecimal defaultLimit);

    @Modifying
    @Query("UPDATE UserLimit ul SET ul.defaultLimit = :newDefaultLimit, ul.updatedAt = CURRENT_TIMESTAMP")
    int updateDefaultLimitForAll(@Param("newDefaultLimit") BigDecimal newDefaultLimit);
}




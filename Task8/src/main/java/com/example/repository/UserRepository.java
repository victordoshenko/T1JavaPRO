package com.example.repository;

import com.example.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Находит пользователя по username
     * @param username имя пользователя
     * @return Optional с пользователем, если найден
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Проверяет существование пользователя по username
     * @param username имя пользователя
     * @return true если пользователь существует
     */
    boolean existsByUsername(String username);
    
    /**
     * Находит всех пользователей, отсортированных по username
     * @return список пользователей
     */
    @Query("SELECT u FROM User u ORDER BY u.username ASC")
    List<User> findAllOrderByUsername();
}


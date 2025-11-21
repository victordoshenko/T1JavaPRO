package com.example.service;

import com.example.User;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(String username) {
        // Проверяем, существует ли пользователь с таким username
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            log.error("Пользователь '{}' уже существует: {}", username, existingUser.get());
            return existingUser.get();
        }
        
        User user = new User();
        user.setUsername(username);
        return userRepository.save(user);
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        log.error("Пользователь с id={} не найден для удаления", id);
        return false;
    }

    public User getUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            log.error("Пользователь с id={} не найден", id);
            return null;
        }
        return user.get();
    }

    public List<User> getAllUsers() {
        return userRepository.findAllOrderByUsername();
    }

    @Transactional
    public void clearAllUsers() {
        userRepository.deleteAll();
    }
}


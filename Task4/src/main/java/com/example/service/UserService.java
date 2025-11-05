package com.example.service;

import com.example.User;
import com.example.dao.UserDao;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUser(String username) {
        // Проверяем, существует ли пользователь с таким username
        User existingUser = userDao.findByUsername(username);
        if (existingUser != null) {
            System.out.println("   Пользователь '" + username + "' уже существует: " + existingUser);
            return existingUser;
        }
        
        User user = new User();
        user.setUsername(username);
        return userDao.create(user);
    }

    public boolean deleteUser(Long id) {
        return userDao.delete(id);
    }

    public User getUser(Long id) {
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public void clearAllUsers() {
        userDao.deleteAll();
    }
}


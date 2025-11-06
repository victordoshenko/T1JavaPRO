package com.example;

import com.example.config.DataSourceConfig;
import com.example.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

@ComponentScan
public class Main {

    public static void main(String[] args) {
        // Создание Spring Context
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                Main.class, DataSourceConfig.class)) {
            // Получение бина UserService
            UserService userService = context.getBean(UserService.class);
            
            System.out.println("=== Начало работы с пользователями ===\n");
            
            // Очистка таблицы для чистой демонстрации
            System.out.println("0. Очистка таблицы для демонстрации:");
            userService.clearAllUsers();
            System.out.println("   Таблица очищена\n");
            
            // Создание пользователей
            System.out.println("1. Создание пользователей:");
            User user1 = userService.createUser("alice");
            System.out.println("   Создан: " + user1);
            
            User user2 = userService.createUser("bob");
            System.out.println("   Создан: " + user2);
            
            User user3 = userService.createUser("charlie");
            System.out.println("   Создан: " + user3);
            System.out.println();
            
            // Получение всех пользователей
            System.out.println("2. Получение всех пользователей:");
            List<User> allUsers = userService.getAllUsers();
            for (User user : allUsers) {
                System.out.println("   " + user);
            }
            System.out.println();
            
            // Получение одного пользователя
            System.out.println("3. Получение пользователя по ID:");
            User foundUser = userService.getUser(user2.getId());
            if (foundUser != null) {
                System.out.println("   Найден: " + foundUser);
            } else {
                System.out.println("   Пользователь не найден");
            }
            System.out.println();
            
            // Попытка получить несуществующего пользователя
            System.out.println("4. Попытка получить несуществующего пользователя (ID=999):");
            User notFound = userService.getUser(999L);
            if (notFound != null) {
                System.out.println("   Найден: " + notFound);
            } else {
                System.out.println("   Пользователь не найден (ожидаемо)");
            }
            System.out.println();
            
            // Удаление пользователя
            System.out.println("5. Удаление пользователя:");
            boolean deleted = userService.deleteUser(user2.getId());
            if (deleted) {
                System.out.println("   Пользователь с ID=" + user2.getId() + " удален");
            } else {
                System.out.println("   Не удалось удалить пользователя");
            }
            System.out.println();
            
            // Получение всех пользователей после удаления
            System.out.println("6. Получение всех пользователей после удаления:");
            allUsers = userService.getAllUsers();
            for (User user : allUsers) {
                System.out.println("   " + user);
            }
            System.out.println();
            
            // Попытка удалить несуществующего пользователя
            System.out.println("7. Попытка удалить несуществующего пользователя (ID=999):");
            deleted = userService.deleteUser(999L);
            if (deleted) {
                System.out.println("   Пользователь удален");
            } else {
                System.out.println("   Пользователь не найден, удаление не выполнено (ожидаемо)");
            }
            System.out.println();
            
            System.out.println("=== Работа завершена ===");
        }
    }
}


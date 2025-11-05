package com.example.dao;

import com.example.User;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserDao {

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User create(User user) {
        String sql = "INSERT INTO users (username) VALUES (?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, user.getUsername());
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                user.setId(rs.getLong("id"));
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user", e);
        }
    }

    public User findById(Long id) {
        String sql = "SELECT id, username FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                return new User(rs.getLong("id"), rs.getString("username"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT id, username FROM users WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                return new User(rs.getLong("id"), rs.getString("username"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT id, username FROM users";
        List<User> users = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(new User(rs.getLong("id"), rs.getString("username")));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }
    }

    public User update(User user) {
        String sql = "UPDATE users SET username = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, user.getUsername());
            statement.setLong(2, user.getId());
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                return user;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM users";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all users", e);
        }
    }
}


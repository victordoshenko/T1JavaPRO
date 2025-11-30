package com.example.repository;

import com.example.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByUserId(Long userId);

    boolean existsByAccountNumber(String accountNumber);
}



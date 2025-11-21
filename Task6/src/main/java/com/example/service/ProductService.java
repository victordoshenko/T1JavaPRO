package com.example.service;

import com.example.User;
import com.example.product.Product;
import com.example.product.ProductType;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Product createProduct(Long userId, String accountNumber, BigDecimal balance, ProductType productType) {
        if (productRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalArgumentException("Product with account number '%s' already exists".formatted(accountNumber));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with id %d not found".formatted(userId)));

        Product product = new Product();
        product.setAccountNumber(accountNumber);
        product.setBalance(balance);
        product.setProductType(productType);
        product.setUser(user);

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByUserId(Long userId) {
        return productRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }
}



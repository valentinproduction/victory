package com.example.victory;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong();

    public ProductService() {
        // Початкові дані для демонстрації
        products.put(counter.incrementAndGet(), new Product(1L, "Laptop", 1200.50));
        products.put(counter.incrementAndGet(), new Product(2L, "Smartphone", 800.00));
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public Optional<Product> getProductById(long id) {
        return Optional.ofNullable(products.get(id));
    }

    public Product createProduct(Product product) {
        long newId = counter.incrementAndGet();
        product.setId(newId);
        products.put(newId, product);
        return product;
    }

    public Optional<Product> updateProduct(long id, Product updatedProduct) {
        if (!products.containsKey(id)) {
            return Optional.empty();
        }
        updatedProduct.setId(id);
        products.put(id, updatedProduct);
        return Optional.of(updatedProduct);
    }

    public boolean deleteProduct(long id) {
        return products.remove(id) != null;
    }
}

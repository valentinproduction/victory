package com.example.victory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Чисті unit-тести бізнес-логіки сховища (без Spring). */
class ProductServiceTest {

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(); // кожен тест отримує свіже сховище з 2 демо-продуктами
    }

    @Test
    void containsSeedProducts() {
        assertEquals(2, service.getAllProducts().size());
    }

    @Test
    void createProduct_assignsNextId() {
        Product created = service.createProduct(new Product(0, "Keyboard", 75.0));

        assertEquals(3L, created.getId());
        assertEquals(3, service.getAllProducts().size());
        assertTrue(service.getProductById(3L).isPresent());
    }

    @Test
    void getProductById_returnsEmptyForUnknownId() {
        assertEquals(Optional.empty(), service.getProductById(99L));
    }

    @Test
    void updateProduct_replacesExistingAndKeepsId() {
        Optional<Product> updated = service.updateProduct(1L, new Product(0, "Gaming Laptop", 1500.0));

        assertTrue(updated.isPresent());
        assertEquals(1L, updated.get().getId());
        assertEquals("Gaming Laptop", service.getProductById(1L).orElseThrow().getName());
    }

    @Test
    void updateProduct_returnsEmptyForUnknownId() {
        assertTrue(service.updateProduct(99L, new Product(0, "Ghost", 1.0)).isEmpty());
        assertEquals(2, service.getAllProducts().size());
    }

    @Test
    void deleteProduct_removesExisting() {
        assertTrue(service.deleteProduct(1L));
        assertFalse(service.getProductById(1L).isPresent());
    }

    @Test
    void deleteProduct_returnsFalseForUnknownId() {
        assertFalse(service.deleteProduct(99L));
        assertEquals(2, service.getAllProducts().size());
    }
}

package com.example.victory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Тестуємо лише контролер, тому сервісний шар підміняємо моком
    @MockitoBean
    private ProductService productService;

    // ---------- GET /products ----------

    @Test
    void getAllProducts_returnsListOfProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(
                new Product(1L, "Laptop", 1200.50),
                new Product(2L, "Smartphone", 800.00)));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Smartphone"));
    }

    @Test
    void getAllProducts_returnsEmptyListWhenCatalogIsEmpty() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllProducts_propagatesServiceFailure() {
        when(productService.getAllProducts()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> mockMvc.perform(get("/products")))
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }

    // ---------- GET /products/{id} ----------

    @Test
    void getProductById_returnsProductWhenFound() throws Exception {
        when(productService.getProductById(1L))
                .thenReturn(Optional.of(new Product(1L, "Laptop", 1200.50)));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1200.50))
                .andExpect(jsonPath("$.dueDate").exists());
    }

    @Test
    void getProductById_returns404WhenNotFound() throws Exception {
        when(productService.getProductById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductById_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/products/abc"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(anyLong());
    }

    // ---------- POST /products ----------

    @Test
    void createProduct_returns201AndCreatedProduct() throws Exception {
        Product saved = new Product(3L, "Keyboard", 75.00);
        when(productService.createProduct(any(Product.class))).thenReturn(saved);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Keyboard\",\"price\":75.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(75.0));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void createProduct_returns400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not a json"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(Product.class));
    }

    @Test
    void createProduct_returns400WhenPriceIsNotANumber() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Keyboard\",\"price\":\"abc\"}"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(Product.class));
    }

    @Test
    void createProduct_returns415WhenContentTypeIsNotJson() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Keyboard"))
                .andExpect(status().isUnsupportedMediaType());

        verify(productService, never()).createProduct(any(Product.class));
    }

    // ---------- PUT /products/{id} ----------

    @Test
    void updateProduct_returns200AndUpdatedProduct() throws Exception {
        when(productService.updateProduct(eq(1L), any(Product.class)))
                .thenReturn(Optional.of(new Product(1L, "Gaming Laptop", 1500.00)));

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Gaming Laptop\",\"price\":1500.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gaming Laptop"))
                .andExpect(jsonPath("$.price").value(1500.0));
    }

    @Test
    void updateProduct_returns404WhenProductDoesNotExist() throws Exception {
        when(productService.updateProduct(eq(99L), any(Product.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ghost\",\"price\":10.0}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_returns400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not a json"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateProduct(anyLong(), any(Product.class));
    }

    // ---------- DELETE /products/{id} ----------

    @Test
    void deleteProduct_returns204WhenDeleted() throws Exception {
        when(productService.deleteProduct(1L)).thenReturn(true);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProduct_returns404WhenProductDoesNotExist() throws Exception {
        when(productService.deleteProduct(99L)).thenReturn(false);

        mockMvc.perform(delete("/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(delete("/products/abc"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).deleteProduct(anyLong());
    }
}


package com.example.productservice.controller;

import com.example.productservice.entity.ProductEntity;
import com.example.productservice.exception.GlobalExceptionHandler;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.model.CreateProductRequest;
import com.example.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldCreateProduct() throws Exception {
        ProductEntity product = createProduct("Keyboard", 10);

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(product);

        mockMvc.perform(post("/products")
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "Keyboard",
                                    "description": "Mechanical keyboard",
                                    "price": 2499.00,
                                    "currency": "INR",
                                    "stockQuantity": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void shouldGetProductById() throws Exception {
        when(productService.getProductById(1L))
                .thenReturn(createProduct("Keyboard", 10));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void shouldListAllProducts() throws Exception {
        when(productService.getAllProducts())
                .thenReturn(List.of(
                        createProduct("Keyboard", 10),
                        createProduct("Mouse", 20)
                ));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Keyboard"))
                .andExpect(jsonPath("$[1].name").value("Mouse"));
    }

    @Test
    void shouldUpdateStock() throws Exception {
        when(productService.updateStock(1L, 25))
                .thenReturn(createProduct("Keyboard", 25));

        mockMvc.perform(patch("/products/1/stock")
                        .contentType("application/json")
                        .content("""
                                {
                                    "stockQuantity": 25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(25));

        verify(productService).updateStock(1L, 25);
    }

    @Test
    void shouldRejectNegativeStock() throws Exception {
        mockMvc.perform(patch("/products/1/stock")
                        .contentType("application/json")
                        .content("""
                                {
                                    "stockQuantity": -5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Stock quantity cannot be negative"));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Product not found with ID: 999"));
    }

    @Test
    void shouldRejectProductWithBlankName() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "",
                                    "price": 100,
                                    "currency": "INR",
                                    "stockQuantity": 5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private ProductEntity createProduct(String name, int stockQuantity) {
        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setDescription("Test product");
        product.setPrice(new BigDecimal("2499.00"));
        product.setCurrency("INR");
        product.setStockQuantity(stockQuantity);
        return product;
    }
}

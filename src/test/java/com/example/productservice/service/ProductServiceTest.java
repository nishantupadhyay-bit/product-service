
package com.example.productservice.service;

import com.example.productservice.entity.ProductEntity;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.model.CreateProductRequest;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Keyboard");
        request.setDescription("Mechanical keyboard");
        request.setPrice(new BigDecimal("2499.00"));
        request.setCurrency("INR");
        request.setStockQuantity(10);

        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductEntity result = productService.createProduct(request);

        assertEquals("Keyboard", result.getName());
        assertEquals("Mechanical keyboard", result.getDescription());
        assertEquals(new BigDecimal("2499.00"), result.getPrice());
        assertEquals("INR", result.getCurrency());
        assertEquals(10, result.getStockQuantity());

        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void shouldReturnProductWhenIdExists() {
        ProductEntity product = new ProductEntity();
        product.setName("Keyboard");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductEntity result = productService.getProductById(1L);

        assertEquals("Keyboard", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        assertEquals(
                "Product not found with ID: 999",
                exception.getMessage()
        );
    }

    @Test
    void shouldReturnAllProducts() {
        ProductEntity keyboard = new ProductEntity();
        keyboard.setName("Keyboard");

        ProductEntity mouse = new ProductEntity();
        mouse.setName("Mouse");

        when(productRepository.findAll())
                .thenReturn(List.of(keyboard, mouse));

        List<ProductEntity> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("Keyboard", result.get(0).getName());
        assertEquals("Mouse", result.get(1).getName());
    }

    @Test
    void shouldUpdateStock() {
        ProductEntity product = new ProductEntity();
        product.setName("Keyboard");
        product.setStockQuantity(10);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductEntity result = productService.updateStock(1L, 25);

        assertEquals(25, result.getStockQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStockForMissingProduct() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateStock(999L, 25)
        );
    }
}

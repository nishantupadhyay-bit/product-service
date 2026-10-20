
package com.example.productservice.service;

import com.example.productservice.entity.ProductEntity;
import com.example.productservice.exception.InsufficientStockException;
import com.example.productservice.exception.InvalidStockQuantityException;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.model.CreateProductRequest;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductEntity product;

    @BeforeEach
    void setUp() {
        product = new ProductEntity();
        product.setName("Laptop");
        product.setDescription("Test laptop");
        product.setPrice(new BigDecimal("50000.00"));
        product.setCurrency("INR");
        product.setStockQuantity(10);
    }

    @Test
    void shouldCreateProduct() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("Test laptop");
        request.setPrice(new BigDecimal("50000.00"));
        request.setCurrency("INR");
        request.setStockQuantity(10);

        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductEntity result = productService.createProduct(request);

        assertEquals("Laptop", result.getName());
        assertEquals("Test laptop", result.getDescription());
        assertEquals(new BigDecimal("50000.00"), result.getPrice());
        assertEquals("INR", result.getCurrency());
        assertEquals(10, result.getStockQuantity());

        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void shouldReturnProductWhenIdExists() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductEntity result = productService.getProductById(1L);

        assertEquals("Laptop", result.getName());
        assertEquals(10, result.getStockQuantity());
        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        assertEquals("Product not found with ID: 999", exception.getMessage());
    }

    @Test
    void shouldReturnAllProducts() {
        ProductEntity secondProduct = new ProductEntity();
        secondProduct.setName("Mouse");
        secondProduct.setPrice(new BigDecimal("500.00"));
        secondProduct.setCurrency("INR");
        secondProduct.setStockQuantity(20);

        when(productRepository.findAll())
                .thenReturn(List.of(product, secondProduct));

        List<ProductEntity> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals("Mouse", result.get(1).getName());

        verify(productRepository).findAll();
    }

    @Test
    void shouldUpdateStock() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductEntity result = productService.updateStock(1L, 15);

        assertEquals(15, result.getStockQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStockForMissingProduct() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateStock(999L, 5)
        );

        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void shouldReserveStockWhenEnoughStockIsAvailable() {
        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductEntity result = productService.reserveStock(1L, 3);

        assertEquals(7, result.getStockQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void shouldRejectReservationWhenStockIsInsufficient() {
        product.setStockQuantity(2);

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> productService.reserveStock(1L, 3)
        );

        assertEquals(
                "Insufficient stock available for product ID: 1",
                exception.getMessage()
        );

        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void shouldReleaseStock() {
        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductEntity result = productService.releaseStock(1L, 2);

        assertEquals(12, result.getStockQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void shouldRejectReservationWithNonPositiveQuantity() {
        assertThrows(
                InvalidStockQuantityException.class,
                () -> productService.reserveStock(1L, 0)
        );

        verify(productRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void shouldRejectReleaseWithNonPositiveQuantity() {
        assertThrows(
                InvalidStockQuantityException.class,
                () -> productService.releaseStock(1L, -1)
        );

        verify(productRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenReservingStockForMissingProduct() {
        when(productRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.reserveStock(999L, 2)
        );

        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenReleasingStockForMissingProduct() {
        when(productRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.releaseStock(999L, 2)
        );

        verify(productRepository, never()).save(any(ProductEntity.class));
    }
}

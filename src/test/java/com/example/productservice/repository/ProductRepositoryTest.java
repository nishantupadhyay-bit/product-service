
package com.example.productservice.repository;

import com.example.productservice.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndRetrieveProduct() {
        ProductEntity product = new ProductEntity();
        product.setName("Keyboard");
        product.setDescription("Mechanical keyboard");
        product.setPrice(new BigDecimal("2499.00"));
        product.setCurrency("INR");
        product.setStockQuantity(10);

        ProductEntity savedProduct = productRepository.saveAndFlush(product);

        Optional<ProductEntity> result =
                productRepository.findById(savedProduct.getId());

        assertTrue(result.isPresent());
        assertEquals("Keyboard", result.get().getName());
        assertEquals(new BigDecimal("2499.00"), result.get().getPrice());
        assertEquals(10, result.get().getStockQuantity());
        assertTrue(result.get().getCreatedAt() != null);
        assertTrue(result.get().getUpdatedAt() != null);
    }
}

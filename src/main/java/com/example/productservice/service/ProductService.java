
package com.example.productservice.service;

import com.example.productservice.entity.ProductEntity;
import com.example.productservice.model.CreateProductRequest;
import com.example.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.productservice.exception.ProductNotFoundException;
import java.util.List;
import com.example.productservice.exception.InsufficientStockException;
import com.example.productservice.exception.InvalidStockQuantityException;

@Service
public class ProductService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductEntity createProduct(CreateProductRequest request) {
        ProductEntity product = new ProductEntity();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCurrency(request.getCurrency().trim().toUpperCase());
        product.setStockQuantity(request.getStockQuantity());

        ProductEntity savedProduct = productRepository.save(product);

        logger.info("Product created successfully with ID: {}",
                savedProduct.getId());

        return savedProduct;
    }

    @Transactional(readOnly = true)
    public ProductEntity getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public ProductEntity updateStock(Long id, int stockQuantity) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setStockQuantity(stockQuantity);

        ProductEntity updatedProduct = productRepository.save(product);

        logger.info("Stock updated for product ID: {}, quantity: {}",
                id, stockQuantity);

        return updatedProduct;
    }



    @Transactional
    public ProductEntity reserveStock(Long id, int quantity) {
        if (quantity <= 0) {
            throw new InvalidStockQuantityException(
                    "Reservation quantity must be greater than zero"
            );
        }

        ProductEntity product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(id);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);

        ProductEntity updatedProduct = productRepository.save(product);
        logger.info("Stock reserved for product ID: {}", id);

        return updatedProduct;
    }

    @Transactional
    public ProductEntity releaseStock(Long id, int quantity) {
        if (quantity <= 0) {
            throw new InvalidStockQuantityException(
                    "Release quantity must be greater than zero"
            );
        }

        ProductEntity product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setStockQuantity(product.getStockQuantity() + quantity);

        ProductEntity updatedProduct = productRepository.save(product);
        logger.info("Stock released for product ID: {}", id);

        return updatedProduct;
    }


}


package com.example.productservice.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long id) {
        super("Insufficient stock available for product ID: " + id);
    }
}

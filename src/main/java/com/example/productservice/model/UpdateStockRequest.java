
package com.example.productservice.model;

import jakarta.validation.constraints.PositiveOrZero;

public class UpdateStockRequest {

    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private int stockQuantity;

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}

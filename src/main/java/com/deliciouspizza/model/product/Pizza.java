package com.deliciouspizza.model.product;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("PIZZA")
public class Pizza extends Product {

    private String pizzaType;

    public Pizza(
            ProductStatus status,
            ProductSize size,
            BigDecimal price,
            boolean isActive,
            BigDecimal totalAmount,
            String pizzaType
    ) {
        super(ProductCategory.PIZZA, status, size, price, isActive, totalAmount);
        this.pizzaType = pizzaType;
    }
}

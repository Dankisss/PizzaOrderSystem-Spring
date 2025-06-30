package com.deliciouspizza.model.product;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("PIZZA")
public class Pizza extends Product {

    public Pizza(
            ProductStatus status,
            String name,
            ProductSize size,
            BigDecimal price,
            boolean isActive,
            BigDecimal totalAmount
    ) {
        super(ProductCategory.PIZZA, name, status, size, price, isActive, totalAmount);
    }

}

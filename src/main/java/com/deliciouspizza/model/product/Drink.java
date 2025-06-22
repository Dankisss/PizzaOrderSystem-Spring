package com.deliciouspizza.model.product;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("DRINK")
public class Drink extends Product {

    private final String drinkType;

    public Drink(
            ProductStatus status,
            ProductSize size,
            BigDecimal price,
            boolean isActive,
            BigDecimal totalAmount,
            String drinkType
    ) {
        super(ProductCategory.PIZZA, status, size, price, isActive, totalAmount);
        this.drinkType = drinkType;
    }

}

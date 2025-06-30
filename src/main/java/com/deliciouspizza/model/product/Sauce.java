package com.deliciouspizza.model.product;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("SAUCE")
public class Sauce extends Product {

    public Sauce() {
        super();
    }

    public Sauce(ProductStatus status, String name, String description, ProductSize size, BigDecimal price, boolean isActive, BigDecimal totalAmount) {
        super(ProductCategory.SAUCE, name, description, status, size, price, isActive, totalAmount);
    }
}

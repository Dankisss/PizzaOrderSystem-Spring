package com.deliciouspizza.model.product;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("SAUCE")
@Getter
@Setter
public class Sauce extends Product {

    public Sauce() {
        super();
    }

    public Sauce(
            ProductStatus status,
            String name,
            String description,
            ProductSize size,
            BigDecimal price,
            boolean isActive,
            BigDecimal totalAmount
    ) {
        super(ProductCategory.SAUCE, name, description, status, size, price, isActive, totalAmount);
    }
}

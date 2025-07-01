package com.deliciouspizza.model.product;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("PIZZA")
@NoArgsConstructor
@Getter
@Setter
public class Pizza extends Product {

    public Pizza(
            ProductStatus status,
            String name,
            String description,
            ProductSize size,
            BigDecimal price,
            boolean isActive,
            BigDecimal totalAmount
    ) {
        super(ProductCategory.PIZZA, name, description, status, size, price, isActive, totalAmount);
    }

}

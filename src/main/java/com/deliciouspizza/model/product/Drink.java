package com.deliciouspizza.model.product;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("DRINK")
@Getter
@Setter
public class Drink extends Product {

    private Boolean alcoholic;

    public Drink(
            ProductStatus status,
            String name,
            ProductSize size,
            BigDecimal price,
            boolean isActive,
            BigDecimal totalAmount,
            boolean alcoholic
    ) {
        super(ProductCategory.PIZZA, name, status, size, price, isActive, totalAmount);
        this.alcoholic = alcoholic;
    }


}

package com.deliciouspizza.dto.product;

import com.deliciouspizza.model.product.ProductCategory;
import com.deliciouspizza.model.product.ProductSize;
import com.deliciouspizza.model.product.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateDto {

    private ProductCategory category;

    private ProductStatus status;
    private ProductSize capacity;

    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    private BigDecimal price;

    private Boolean active;
    private BigDecimal totalAmount;

    private String pizzaType;
    private String drinkType;
    private Boolean isAlcoholic;

    private String name;
    private String description;

}

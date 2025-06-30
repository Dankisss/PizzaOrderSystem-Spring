package com.deliciouspizza.dto.product;

import com.deliciouspizza.model.product.ProductCategory;
import com.deliciouspizza.model.product.ProductSize;
import com.deliciouspizza.model.product.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductInputDto {

    private ProductCategory category;
    private ProductStatus status;
    private ProductSize size;
    private BigDecimal price;
    private boolean active;
    private BigDecimal totalAmount;
    private String name;
    private String description;

    private Boolean alcoholic;
}

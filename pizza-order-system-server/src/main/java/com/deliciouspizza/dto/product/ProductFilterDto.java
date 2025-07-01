package com.deliciouspizza.dto.product;

import com.deliciouspizza.model.product.ProductCategory;
import com.deliciouspizza.model.product.ProductSize;
import com.deliciouspizza.model.product.ProductStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductFilterDto {

    private ProductCategory category;
    private ProductStatus status;
    private ProductSize size;
    private Boolean active;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

}


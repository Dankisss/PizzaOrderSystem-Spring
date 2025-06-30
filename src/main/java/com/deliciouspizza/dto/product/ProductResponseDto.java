package com.deliciouspizza.dto.product;

import com.deliciouspizza.model.product.ProductCategory;
import com.deliciouspizza.model.product.ProductSize;
import com.deliciouspizza.model.product.ProductStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
public class ProductResponseDto {

    private Long id;
    private ProductCategory category;
    private ProductStatus status;
    private ProductSize size;
    private BigDecimal price;
    private Boolean active; // Consistent with update DTO
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;

    private String name;
    private Boolean isAlcoholic;

    private Boolean hasImage;

    private String description;
}
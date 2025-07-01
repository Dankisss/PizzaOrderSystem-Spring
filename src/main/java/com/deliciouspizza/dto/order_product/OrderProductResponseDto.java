package com.deliciouspizza.dto.order_product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderProductResponseDto {

    private long orderId;
    private long productId;
    private Integer quantity;
    private BigDecimal priceAtOrderTime;

}
package com.deliciouspizza.dto.order;

import com.deliciouspizza.dto.order_product.OrderProductResponseDto;
import com.deliciouspizza.model.orders_products.OrderProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    private Long id;
    private String status;
    private Long userId; // Consider UserResponseDTO for full user details
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderProductResponseDto> items;

}

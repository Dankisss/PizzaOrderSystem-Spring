package com.deliciouspizza.dto.order;

import com.deliciouspizza.dto.order_product.OrderProductRequestDto;
import com.deliciouspizza.dto.order_product.OrderProductResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {

    @NotBlank(message = "Address is required")
    @Size(max = 50, message = "Address cannot exceed 50 characters")
    private String address;

    @NotNull
    private long userId;

    @Valid
    @NotNull(message = "Order must contain at least one item")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderProductRequestDto> items;

}
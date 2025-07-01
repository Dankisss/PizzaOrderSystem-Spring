package com.deliciouspizza.dto.order;

import com.deliciouspizza.model.order.OrderStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderUpdateDto {

    @Pattern(regexp = "NEW|PROCESSING|COMPLETED|CANCELLED", message = "Invalid order status")
    private String status;

    @Size(max = 50, message = "Address cannot exceed 50 characters")
    private String address;

}

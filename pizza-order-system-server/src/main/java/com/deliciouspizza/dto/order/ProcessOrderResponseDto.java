package com.deliciouspizza.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessOrderResponseDto {

    private OrderResponseDto order;
    private String distance;
    private String time;

}
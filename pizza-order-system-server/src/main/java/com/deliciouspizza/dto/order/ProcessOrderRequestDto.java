package com.deliciouspizza.dto.order;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessOrderRequestDto {
    @NotNull
    private Long employeeId;
}

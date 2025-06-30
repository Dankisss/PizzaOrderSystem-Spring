package com.deliciouspizza.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDto {

    private String username;

    @Email(message = "Please provide a valid email address")
    private String email;

}

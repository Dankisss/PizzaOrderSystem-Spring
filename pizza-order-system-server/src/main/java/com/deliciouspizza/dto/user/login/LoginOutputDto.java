package com.deliciouspizza.dto.user.login;

import lombok.Data;

@Data
public class LoginOutputDto {
    private String token;

    public LoginOutputDto(String token) {
        this.token = token;
    }

}

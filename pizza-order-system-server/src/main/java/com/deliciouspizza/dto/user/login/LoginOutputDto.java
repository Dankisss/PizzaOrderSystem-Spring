package com.deliciouspizza.dto.user.login;

import lombok.Data;

@Data
public class LoginOutputDto {
    private String token;
    private String role;
    private Long userId;

    public LoginOutputDto(String token, String role, Long userId) {
        this.token = token;
        this.role = role;
        this.userId = userId;
    }

}

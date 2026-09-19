package com.greenboard.investman.vo.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthRequestVO {
    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "password is required")
    private String password;
}

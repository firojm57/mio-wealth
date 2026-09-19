package com.greenboard.investman.vo.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRegisterVO {
    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "password is required")
    private String password;

    @NotBlank(message = "firstName is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "lastName is required")
    private String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "valid email is required")
    private String email;

    private String mobile;
}

package com.greenboard.investman.vo.auth;

import com.greenboard.investman.vo.user.UserProfileVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseVO {
    private String token;
    private String tokenType = "Bearer";
    private String userId;
    private UserProfileVO userProfile;

    public AuthResponseVO(String token, String userId, UserProfileVO userProfile) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.userProfile = userProfile;
    }
}

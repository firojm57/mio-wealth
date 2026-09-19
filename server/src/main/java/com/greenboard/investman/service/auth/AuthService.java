package com.greenboard.investman.service.auth;

import com.greenboard.investman.vo.auth.AuthRequestVO;
import com.greenboard.investman.vo.auth.AuthResponseVO;
import com.greenboard.investman.vo.auth.UserRegisterVO;
import com.greenboard.investman.vo.common.StatusVO;

public interface AuthService {
    AuthResponseVO login(AuthRequestVO request);
    StatusVO register(UserRegisterVO request);
}

package com.greenboard.investman.controller.auth;

import com.greenboard.investman.service.auth.AuthService;
import com.greenboard.investman.vo.auth.AuthRequestVO;
import com.greenboard.investman.vo.auth.AuthResponseVO;
import com.greenboard.investman.vo.auth.UserRegisterVO;
import com.greenboard.investman.vo.common.StatusVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseVO> login(@Valid @RequestBody AuthRequestVO request) {
        log.info("Processing login request for user '{}'", request.getUserId());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<StatusVO> register(@Valid @RequestBody UserRegisterVO request) {
        log.info("Processing registration request for user '{}'", request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}

package com.greenboard.investman.controller.auth;

import com.greenboard.investman.service.auth.AuthService;
import com.greenboard.investman.vo.auth.AuthRequestVO;
import com.greenboard.investman.vo.auth.AuthResponseVO;
import com.greenboard.investman.vo.auth.UserRegisterVO;
import com.greenboard.investman.vo.common.StatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "1. Authentication", description = "User registration, credential verification, and JWT token issuance")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates user credentials and returns a signed 24-hour JWT token containing the tenant schema claim.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid user ID or password")
    })
    public ResponseEntity<AuthResponseVO> login(@Valid @RequestBody AuthRequestVO request) {
        log.info("Processing login request for user '{}'", request.getUserId());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Creates a new user login account in the master schema, provisions an isolated tenant schema, and seeds the initial user profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered and tenant provisioned successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failure or user ID already exists")
    })
    public ResponseEntity<StatusVO> register(@Valid @RequestBody UserRegisterVO request) {
        log.info("Processing registration request for user '{}'", request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}

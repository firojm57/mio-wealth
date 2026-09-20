package com.greenboard.investman.service.auth.impl;

import com.greenboard.investman.model.user.User;
import com.greenboard.investman.multitenancy.TenantProvisioningService;
import com.greenboard.investman.repository.user.UserRepository;
import com.greenboard.investman.security.JwtTokenProvider;
import com.greenboard.investman.service.auth.AuthService;
import com.greenboard.investman.util.APIConstants;
import com.greenboard.investman.vo.auth.AuthRequestVO;
import com.greenboard.investman.vo.auth.AuthResponseVO;
import com.greenboard.investman.vo.auth.UserRegisterVO;
import com.greenboard.investman.vo.common.StatusVO;
import com.greenboard.investman.vo.user.UserProfileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TenantProvisioningService tenantProvisioningService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           TenantProvisioningService tenantProvisioningService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tenantProvisioningService = tenantProvisioningService;
    }

    @Override
    public AuthResponseVO login(AuthRequestVO request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Invalid user ID or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid user ID or password.");
        }

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getTenantSchema());

        UserProfileVO profileVO = tenantProvisioningService.getTenantProfile(user.getTenantSchema());
        if (profileVO == null) {
            profileVO = new UserProfileVO();
            profileVO.setFirstName(user.getUserId());
            profileVO.setLastName("");
            profileVO.setEmail("");
        }

        log.info("User '{}' on tenant '{}' authenticated successfully via JWT", user.getUserId(), user.getTenantSchema());
        return new AuthResponseVO(token, user.getUserId(), profileVO);
    }

    @Override
    public StatusVO register(UserRegisterVO request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User ID already exists.");
        }

        // 1. Generate unique schema name: "tenant_" + 16 random hex chars
        String tenantSchema = "tenant_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 2. Save credentials in public.user_login
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUserId(), encodedPassword, tenantSchema);
        userRepository.save(user);

        // 3. Physically create schema and run Flyway tenant migrations
        tenantProvisioningService.provisionTenant(tenantSchema);

        // 4. Seed initial profile directly in the tenant schema
        tenantProvisioningService.initTenantProfile(
                tenantSchema,
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName(),
                request.getEmail(),
                request.getMobile()
        );

        log.info("Successfully registered user '{}' and provisioned schema '{}'", request.getUserId(), tenantSchema);

        StatusVO status = new StatusVO();
        status.setStatus(APIConstants.LOGIN_STATUS_SUCCESS);
        return status;
    }
}

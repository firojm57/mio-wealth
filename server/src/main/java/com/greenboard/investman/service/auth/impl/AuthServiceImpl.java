package com.greenboard.investman.service.auth.impl;

import com.greenboard.investman.model.user.User;
import com.greenboard.investman.model.user.UserProfile;
import com.greenboard.investman.repository.user.UserProfileRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           UserProfileRepository userProfileRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponseVO login(AuthRequestVO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Invalid user ID or password."));

        // Support BCrypt hashed passwords, while allowing smooth migration for legacy accounts
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword())
                || request.getPassword().equals(user.getPassword());

        if (!matches) {
            throw new BadCredentialsException("Invalid user ID or password.");
        }

        String token = jwtTokenProvider.generateToken(user.getUserId());

        UserProfile profile = userProfileRepository.findByUser_UserId(user.getUserId()).orElse(null);
        UserProfileVO profileVO = new UserProfileVO();
        if (profile != null) {
            profileVO.setFirstName(profile.getFirstName());
            profileVO.setMiddleName(profile.getMiddleName());
            profileVO.setLastName(profile.getLastName());
            profileVO.setEmail(profile.getEmail());
            profileVO.setMobile(profile.getMobile());
        } else {
            profileVO.setFirstName(user.getUserId());
            profileVO.setLastName("User");
            profileVO.setEmail(user.getUserId() + "@miowealth.local");
        }

        log.info("User '{}' authenticated successfully via JWT", user.getUserId());
        return new AuthResponseVO(token, user.getUserId(), profileVO);
    }

    @Override
    @Transactional
    public StatusVO register(UserRegisterVO request) {
        if (userRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("User ID already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUserId(), encodedPassword);
        userRepository.save(user);

        UserProfile profile = new UserProfile(
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName(),
                request.getEmail(),
                request.getMobile(),
                user
        );
        userProfileRepository.save(profile);

        log.info("Successfully registered new user '{}' (email: {})", request.getUserId(), request.getEmail());

        StatusVO status = new StatusVO();
        status.setStatus(APIConstants.LOGIN_STATUS_SUCCESS);
        return status;
    }
}

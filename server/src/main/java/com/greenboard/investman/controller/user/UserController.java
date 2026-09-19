package com.greenboard.investman.controller.user;

import com.greenboard.investman.service.user.UserProfileService;
import com.greenboard.investman.vo.user.UserProfileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileVO> getCurrentUserProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Fetching profile for user '{}'", principal.getName());
        UserProfileVO profile = userProfileService.getUserProfileByUserId(principal.getName());
        if (profile == null) {
            log.warn("Profile not found for user '{}'", principal.getName());
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }
}

package com.greenboard.investman.service.user.impl;

import com.greenboard.investman.model.user.UserProfile;
import com.greenboard.investman.repository.user.UserProfileRepository;
import com.greenboard.investman.service.user.UserProfileService;
import com.greenboard.investman.util.convertor.UserModelConvertor;
import com.greenboard.investman.vo.user.UserProfileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    private final UserProfileRepository profileRepository;

    public UserProfileServiceImpl(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileVO getUserProfileByUserId(String userId) {
        UserProfile profile = profileRepository.findByUser_UserId(userId).orElse(null);
        return UserModelConvertor.toUserProfileVO(profile);
    }
}

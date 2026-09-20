package com.greenboard.investman.service.user.impl;

import com.greenboard.investman.model.user.UserProfile;
import com.greenboard.investman.repository.user.UserProfileRepository;
import com.greenboard.investman.service.user.UserProfileService;
import com.greenboard.investman.util.convertor.UserModelConvertor;
import com.greenboard.investman.vo.user.UserProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;

    public UserProfileServiceImpl(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileVO getUserProfileByUserId(String userId) {
        UserProfile profile = profileRepository.findTopByOrderByIdAsc().orElse(null);
        return UserModelConvertor.toUserProfileVO(profile);
    }
}

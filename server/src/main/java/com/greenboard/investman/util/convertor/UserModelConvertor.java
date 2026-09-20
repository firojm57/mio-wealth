package com.greenboard.investman.util.convertor;

import com.greenboard.investman.model.user.UserProfile;
import com.greenboard.investman.vo.user.AddressVO;
import com.greenboard.investman.vo.user.UserProfileVO;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserModelConvertor {

    private UserModelConvertor() {
        // Utility class
    }

    public static UserProfileVO toUserProfileVO(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        Set<AddressVO> addresses = Collections.emptySet();
        try {
            if (profile.getAddresses() != null) {
                addresses = profile.getAddresses().stream()
                        .map(address -> new AddressVO(
                                address.getLine1(),
                                address.getLine2(),
                                address.getCity(),
                                address.getState(),
                                address.getCountry(),
                                address.getPostalCode()))
                        .collect(Collectors.toSet());
            }
        } catch (Exception ignored) {
            // Guard against uninitialized lazy collection
        }

        return new UserProfileVO(
                profile.getFirstName(),
                profile.getMiddleName(),
                profile.getLastName(),
                profile.getEmail(),
                profile.getMobile(),
                addresses
        );
    }
}

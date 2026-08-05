package com.healthcare.aiservice.security.service.interfacies;

import com.healthcare.aiservice.security.dto.UserAuthInfoDto;

public interface UserAuthInfoService {

    UserAuthInfoDto getUserAuthInfoByUserId(long userId);
}

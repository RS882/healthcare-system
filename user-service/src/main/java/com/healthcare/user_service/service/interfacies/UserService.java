package com.healthcare.user_service.service.interfacies;


import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserInfoDto;

public interface UserService {

    UserInfoDto getUserInfoByEmail(String email);
    UserInfoDto registration(RegistrationDto dto);
}

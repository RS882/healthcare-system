package com.healthcare.user_service.service.interfacies;


import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserInfoDto;
import com.healthcare.user_service.model.dto.UserRegDto;

public interface UserService {

    UserInfoDto getUserInfoByEmail(String email);
    UserRegDto registration(RegistrationDto dto);
}

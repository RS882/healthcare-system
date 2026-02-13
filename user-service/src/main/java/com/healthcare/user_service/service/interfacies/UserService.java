package com.healthcare.user_service.service.interfacies;


import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserAuthDto;
import com.healthcare.user_service.model.dto.UserDto;

public interface UserService {

    UserAuthDto getUserInfoByEmail(String email);
    UserDto registration(RegistrationDto dto);
}

package com.healthcare.user_service.service.interfacies;


import com.healthcare.user_service.model.dto.request.RegistrationDto;
import com.healthcare.user_service.model.dto.response.RegistrationResponse;
import com.healthcare.user_service.model.dto.auth.UserAuthDto;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;

public interface UserService {

    RegistrationResponse registration(RegistrationDto dto);

    UserAuthDto getUserInfoByEmail(String email);

    UserAuthInfoDto getUserAuthInfoDtoById(Long id);

}

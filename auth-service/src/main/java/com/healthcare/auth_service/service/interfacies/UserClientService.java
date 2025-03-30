package com.healthcare.auth_service.service.interfacies;

import com.healthcare.auth_service.domain.AuthUserDetails;

public interface UserClientService {

    AuthUserDetails getUserByEmail(String email);

}

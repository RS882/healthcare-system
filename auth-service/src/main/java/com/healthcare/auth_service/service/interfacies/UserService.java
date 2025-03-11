package com.healthcare.auth_service.service.interfacies;

import com.healthcare.auth_service.domain.model.User;

public interface UserService {

    User registration(User user);
    User findByUsername(String name);
}

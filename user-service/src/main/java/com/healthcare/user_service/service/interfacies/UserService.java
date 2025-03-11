package com.healthcare.user_service.service.interfacies;


import com.healthcare.user_service.model.User;

public interface UserService {

    User registration(User user);
    User findByUsername(String name);
}

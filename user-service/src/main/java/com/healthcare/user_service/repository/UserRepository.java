package com.healthcare.user_service.repository;


import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
       select ur.role
       from UserRole ur
       where ur.user.id = :userId
       """)
    Set<Role> findRolesByUserId(Long userId);
}

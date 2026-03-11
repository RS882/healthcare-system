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
       select distinct ur.role
       from UserRole ur
       join ur.user u
       where u.id = :userId
         and u.isActive = true
       """)
    Set<Role> findRolesByUserIdIfUserActive(Long userId);

    Optional<User> findById(Long id);
}

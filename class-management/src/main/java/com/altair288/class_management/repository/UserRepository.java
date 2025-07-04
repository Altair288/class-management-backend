// src/main/java/com/altair288/class_management/repository/UserRepository.java
package com.altair288.class_management.repository;

import com.altair288.class_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByIdentityNo(String identityNo);
    // Optional<User> findByIdentityNo(String identityNo);

    // 新增：用户名或学号任意一个匹配即可
    @Query("SELECT u FROM User u WHERE u.username = :login OR u.identityNo = :login")
    Optional<User> findByUsernameOrIdentityNo(@Param("login") String login);
}
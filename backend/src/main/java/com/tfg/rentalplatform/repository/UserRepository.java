package com.tfg.rentalplatform.repository;

import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findFirstByRoleAndActiveTrueOrderByIdAsc(UserRole role);
    boolean existsByEmail(String email);
}

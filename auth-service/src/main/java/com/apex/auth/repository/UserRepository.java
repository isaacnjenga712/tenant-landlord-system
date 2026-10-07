package com.apex.auth.repository;

import com.apex.auth.entity.Role;
import com.apex.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long>,
                                        JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPublicId(UUID publicId);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    long countByEnabled(boolean enabled);

    long countByRoleAndEnabled(Role role, boolean enabled);
}
